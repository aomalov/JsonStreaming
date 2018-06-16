package services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import model.InputData
import play.api.libs.json.Json

import scala.io.{Source => IOSource}
import scala.util.{Failure, Success, Try}

object StreamRunner extends App {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  def runStream = {

    val data: Source[String,NotUsed] = Source.fromIterator(()=>IOSource.fromResource("json-stream-real.txt").getLines())

    val flow  = Flow[String]
      .map { str =>
        Try(Json.parse(str).as[InputData])
      }
//      .map {
//        case Success(input) => input.toString
//        case Failure(ex) => s"Error input line [${ex.getMessage}]"
//      }

    //TODO - conflate on every second - to send updates to Actor who archives the running results
    //Send HTTP requests to the Actor, for he got eventual results
    data.via(flow).via(new StatsAggregator()).runForeach(println)
      .onComplete(_=>actorSystem.terminate)
  }

  runStream
}
