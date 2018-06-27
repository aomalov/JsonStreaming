package flow

import akka.actor.{Actor, ActorLogging, Terminated}
import play.api.Logger
import play.api.libs.json.Json

import scala.collection.mutable

class StatsArchiveActor extends Actor with ActorLogging {

  def receive: Receive = statsActions(Map(EventStatsRequest->Map.empty[String,Long],WordStatsRequest->Map.empty[String,Long]))

  def statsActions(data: Map[StatsRequest,Map[String,Long]]): Receive = {
    case EventsUpdate(map) =>
      chageState(EventStatsRequest,map,data)

    case WordsUpdate(map) =>
      chageState(WordStatsRequest,map,data)

    case requestSignal:StatsRequest=>
      sender() ! data(requestSignal).toJson

    case Terminated =>
      context stop self
  }

  private def chageState(request: StatsRequest, map:Map[String,Long],data: Map[StatsRequest,Map[String,Long]]): Unit = {
    Logger.debug(s"[Archiver] got update $map")
    val updatedEventStats=map.foldLeft(data(request)) {
      case (m,(key,value))=> m.updated(key,m.getOrElse(key,0L)+value)
    }
    context.become(statsActions(data++Map(request->updatedEventStats)))
  }
}

sealed trait StatsUpdate {
  def map: Map[String,Long]
}

case class EventsUpdate(map: Map[String, Long]) extends StatsUpdate
case class WordsUpdate(map: Map[String, Long]) extends StatsUpdate


sealed trait StatsRequest

case object EventStatsRequest extends StatsRequest
case object WordStatsRequest extends StatsRequest