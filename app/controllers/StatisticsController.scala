package controllers

import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import flow.{EventStatsRequest, StatsRequest, WordStatsRequest}
import javax.inject._
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{AkkaManagement, JStreamer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps


@Singleton
class StatisticsController @Inject()(cc: ControllerComponents,
                                     actorSystem: ActorSystem,
                                     config: Configuration,
                                     akkaManagement: AkkaManagement,
                                     streamer: JStreamer,
                                     )(implicit exec: ExecutionContext) extends AbstractController(cc) {
  implicit val timeout: Timeout = config.get[Int]("app.request.timeout") milliseconds

  private def askArchiver(requestType: StatsRequest) = Action.async {
    akkaManagement.getArchiverActor.ask(requestType)
      .mapTo[JsValue].map(Ok(_))
      .recover {
        case ex => Ok(s"Stats $requestType currently not available [${ex.getMessage}]")
      }
  }

  def getEventCount = askArchiver(EventStatsRequest)

  def getWordCount = askArchiver(WordStatsRequest)

}
