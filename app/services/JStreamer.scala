package services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import com.google.inject.ImplementedBy
import flow._
import javax.inject._
import model.InputData
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Try


@ImplementedBy(classOf[JStreamerImpl])
trait JStreamer {
  def startStream()
}

@Singleton
class JStreamerImpl @Inject()(inputStream: JsonInputStream,
                              implicit val actorSystem: ActorSystem,
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

    val ticker = Source.tick(5.millis, 1.second, 1)

    val statsFlow = inputStream.source
      .via(akkaManagement.getShutdownSwitch.flow)
      .via(flow)
      .via(new StatsAggregator(_.event_type))
      .conflateWithSeed(tuple => Map(tuple._1 -> 1L)) { (m, tuple) =>
        m + (tuple._1 -> (m.getOrElse(tuple._1, 0L) + 1L))
      }

    //TODO add words statistics (parallel or together ?)
    ticker.zip(statsFlow).runForeach {
      case (_, map) =>
        archiverRef ! EventsUpdate(map)
    }
      .flatMap(_ =>actorSystem.terminate())

  }


}


