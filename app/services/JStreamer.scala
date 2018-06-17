package services

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, Inlet, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}
import GraphDSL.Implicits._
import akka.Done
import com.google.inject.ImplementedBy
import flow._
import javax.inject._
import model.InputData
import play.api.Configuration
import play.api.libs.json.Json
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

  override def startStream(): Unit = {

    //TODO can be split into X parsing fan-out stage - to get messages parsed in parallel (order is not important)
    val flow = Flow[String]
      .map { str =>
        Try(Json.parse(str).as[InputData])
      }

    val archiverRef = akkaManagement.getArchiverActor

    val ticker = Source.tick(5.millis, config.get[Int]("app.throttling.rate") milliseconds, 1)

    val graph = RunnableGraph.fromGraph( GraphDSL.create() { implicit builder =>
      val splitterData = builder.add(Broadcast[Try[InputData]](2))
      val splitterTicks = builder.add(Broadcast[Int](2))

      val throttleEventStats = builder.add(Zip[Int,Map[String,Long]])
      val throttleWordStats = builder.add(Zip[Int,Map[String,Long]])

      ticker ~> splitterTicks ~> throttleEventStats.in0
                splitterTicks ~> throttleWordStats.in0

      inputStream.source
        .via(akkaManagement.getShutdownSwitch.flow)
        .via(flow) ~> splitterData

      splitterData ~> StatsAggregator(_.event_type) ~>
        Flow[Tuple[String,Long]].conflateWithSeed(tuple => Map(tuple._1 -> 1L)) { (m, tuple) =>
          m + (tuple._1 -> (m.getOrElse(tuple._1, 0L) + 1L))
        } ~> throttleEventStats.in1

      splitterData ~> StatsAggregator(_.data) ~>
        Flow[Tuple[String,Long]].conflateWithSeed(tuple => Map(tuple._1 -> 1L)) { (m, tuple) =>
          m + (tuple._1 -> (m.getOrElse(tuple._1, 0L) + 1L))
        } ~> throttleWordStats.in1

      throttleEventStats.out ~> Sink.foreach[(Int,Map[String,Long])]
      {
        case (_, map) =>
          archiverRef ! EventsUpdate(map)
      }

      throttleWordStats.out ~> Sink.foreach[(Int,Map[String,Long])]
      {
        case (_, map) =>
          archiverRef ! WordsUpdate(map)
      }

      ClosedShape
    })

    graph.run
  }


}


