package controllers

import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import flow.{EventStatsRequest, StatsRequest, WordStatsRequest}
import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.libs.json._
import services.{AkkaManagement, JStreamer}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._


@Singleton
class StatisticsController @Inject()(cc: ControllerComponents,
                                     actorSystem: ActorSystem,
                                     akkaManagement: AkkaManagement,
                                     streamer: JStreamer,
                                     )(implicit exec: ExecutionContext) extends AbstractController(cc) {
  //TODO set up Timeout in configs
  implicit val timeout: Timeout = 3.seconds

  private def askArchiver(requestType: StatsRequest) = Action.async {
    akkaManagement.getArchiverActor.ask(requestType)
      .mapTo[JsValue].map(Ok(_))
      .recover {
        case ex => Ok(s"Stats $requestType currently not available [${ex.getMessage}]")
      }
  }

  def getEventCount = askArchiver(EventStatsRequest)

  def getWordCount = askArchiver(WordStatsRequest)

  def stopStats = Action {
    akkaManagement.getShutdownSwitch.shutdown()
    Ok("stopped")
  }
}
