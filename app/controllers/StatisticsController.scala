package controllers

import akka.actor.ActorSystem
import javax.inject._
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.libs.json._
import services.JStreamer

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StatisticsController @Inject()(cc: ControllerComponents,
                                     actorSystem: ActorSystem,
                                     streamer: JStreamer,
                                     )(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def getEventCount = Action.async {
    Future(Ok(Json.toJson(streamer.getEventCount)))
  }

  def getWordCount = Action.async {
    Future(Ok(Json.toJson(streamer.getWordSummary)))
  }

}
