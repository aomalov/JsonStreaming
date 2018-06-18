package services

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}
import com.google.inject.ImplementedBy
import flow._
import javax.inject._
import model.InputData
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import play.libs.F.Tuple

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try


@ImplementedBy(classOf[JStreamerImpl])
trait JStreamer {
  def startStream()
}

@Singleton
class JStreamerImpl @Inject()(inputStream: JsonInputStream,
                              implicit val actorSystem: ActorSystem,
                              config: Configuration,
                              akkaManagement: AkkaManagement) extends JStreamer {

  implicit val mat = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  //TODO may be split into X parsing fan-out stage - to get messages parsed in parallel (order is not important)
  private val parsedFlow = Flow[String]
    .map { str =>
      Logger.debug(s"Got message [$str]")
      Try(Json.parse(str).as[InputData])
    }

  private val archiverRef = akkaManagement.getArchiverActor


  private def simpleGraph = {
    RunnableGraph.fromGraph( GraphDSL.create() { implicit builder =>
      val splitterData = builder.add(Broadcast[Try[InputData]](2))

      inputStream.source
        .via(akkaManagement.getShutdownSwitch.flow)
        .via(parsedFlow) ~> splitterData

      splitterData ~> StatsAggregator(_.event_type) ~>
        Sink.foreach[Tuple[String,Long]](tuple => archiverRef ! EventsUpdate(Map(tuple._1->tuple._2)))

      splitterData ~> StatsAggregator(_.data) ~>
        Sink.foreach[Tuple[String,Long]](tuple => archiverRef ! WordsUpdate(Map(tuple._1->tuple._2)))

      ClosedShape
    })
  }

  private def speedyGraph ={
    val statsUpdatePeriod=config.get[Int]("app.throttling.rate") milliseconds

    val ticker = Source.tick(5.millis,  statsUpdatePeriod, 1)

    RunnableGraph.fromGraph( GraphDSL.create() { implicit builder =>
      val splitterData = builder.add(Broadcast[Try[InputData]](2))
      val splitterTicks = builder.add(Broadcast[Int](2))

      val zipEventStats = builder.add(Zip[Int,Map[String,Long]])
      val zipWordStats = builder.add(Zip[Int,Map[String,Long]])

      ticker ~> splitterTicks ~> zipEventStats.in0
      splitterTicks ~> zipWordStats.in0

      inputStream.source
        .via(akkaManagement.getShutdownSwitch.flow)
        .via(parsedFlow) ~> splitterData

      splitterData ~> StatsAggregator(_.event_type) ~>
        Flow[Tuple[String,Long]]
                .conflateWithSeed(tuple => Map(tuple._1 -> 1L)) { (m, tuple) =>
                m + (tuple._1 -> (m.getOrElse(tuple._1, 0L) + 1L))
              } ~> zipEventStats.in1

      splitterData ~> StatsAggregator(_.data) ~>
        Flow[Tuple[String,Long]]
                .conflateWithSeed(tuple => Map(tuple._1 -> 1L)) { (m, tuple) =>
                m + (tuple._1 -> (m.getOrElse(tuple._1, 0L) + 1L))
              } ~> zipWordStats.in1

      zipEventStats.out ~> Sink.foreach[(Int,Map[String,Long])]
        {
          case (_, map) =>
            archiverRef ! EventsUpdate(map)
        }

      zipWordStats.out ~> Sink.foreach[(Int,Map[String,Long])]
        {
          case (_, map) =>
            archiverRef ! WordsUpdate(map)
        }

      ClosedShape
    })

  }

  override def startStream(): Unit = {

    if (config.get[Boolean]("app.design.fast-producer")) {
      speedyGraph.run
    } else {
      simpleGraph.run
    }
  }


}


