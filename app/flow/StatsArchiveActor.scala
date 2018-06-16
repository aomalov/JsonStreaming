package flow

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import play.api.libs.json.Json

import scala.collection.mutable

class StatsArchiveActor extends Actor with ActorLogging {

  var eventStats: mutable.Map[String, Long] = mutable.HashMap.empty[String, Long] withDefaultValue 0
  var wordStats: mutable.Map[String, Long] = mutable.HashMap.empty[String, Long] withDefaultValue 0

  def receive = {
    case EventsUpdate(map) =>
      log.info(s"Archiver: got events update $map")
      map.foreach {
        case (event, count) => eventStats.update(event, eventStats.getOrElse(event, 0L) + count)
      }

    case WordsUpdate(map) =>
      map.foreach {
        case (word, count) => wordStats.update(word, wordStats.getOrElse(word, 0L) + count)
      }

    case EventStatsRequest =>
      log.info("Archiver: got events request")
      sender() ! Json.toJson(eventStats)
  }
}

sealed trait StatsUpdate

case class EventsUpdate(map: Map[String, Long]) extends StatsUpdate

case class WordsUpdate(map: Map[String, Long]) extends StatsUpdate

sealed trait StatsRequest

case object EventStatsRequest extends StatsRequest

case object WordStatsRequest extends StatsRequest