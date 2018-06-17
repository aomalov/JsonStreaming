package services

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import flow.{EventStatsRequest, EventsUpdate, StatsAggregator, StatsArchiveActor}
import model.InputData
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.io.{Source => IOSource}
import scala.util.Try

object StreamRunner extends App {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def runStream(): Unit = {

    val data: Source[String,NotUsed] = Source.fromIterator(()=>IOSource.fromResource("json-stream-real.txt").getLines())

    val flow  = Flow[String]
      .map { str =>
        Try(Json.parse(str).as[InputData])
      }

    //TODO - conflate on every second - to send updates to Actor who archives the running results
    //Send HTTP requests to the Actor, for he got eventual results

    val archiverRef=actorSystem.actorOf(Props[StatsArchiveActor])

    val ticker=Source.tick(5.millis,1.second,1)

    val statsFlow = data
      .via(flow)
      .via(new StatsAggregator(_.event_type))
      .conflateWithSeed(tuple=>Map(tuple._1->1L)) { (m,tuple) =>
        m+(tuple._1->(m.getOrElse(tuple._1,0L)+1L))
      }

    ticker.zip(statsFlow).runForeach {
      case (_,map) =>
        archiverRef ! EventsUpdate(map)
//        println(map)
    }
      .onComplete(_=> {
        archiverRef.ask(EventStatsRequest)(1.second).mapTo[JsValue]
            .transformWith { js=>
              println(js.toString)
              actorSystem.terminate
            }
      })

  }

  runStream
}
