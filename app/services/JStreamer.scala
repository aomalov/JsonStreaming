package services

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Sink, Source}
import javax.inject._
import model.InputData
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json

trait JStreamer {
  def getWordSummary:Seq[WordSummary]
  def getEventCount:Long
}

case class WordSummary(word: String, count: Long)

case class EventSummary(event_type: String, count: Long)



@Singleton
class JStreamerImpl @Inject() (appLifecycle: ApplicationLifecycle,
//                               inputStream: Stream[String],
                               actorSystem: ActorSystem
                               ) extends JStreamer {

  override def getWordSummary: Seq[WordSummary] = {
    List(WordSummary("lorem",1),WordSummary("ipsum",2))
  }

  override def getEventCount: Long = {
    15
  }


}


