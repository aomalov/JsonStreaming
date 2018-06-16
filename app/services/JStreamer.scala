package services

import akka.actor.ActorSystem
import javax.inject._
import play.api.inject.ApplicationLifecycle

trait JStreamer {
  def getWordSummary:Seq[WordSummary]
  def getEventCount:Seq[EventSummary]
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

  override def getEventCount: Seq[EventSummary] = {
    List(EventSummary("foo",1),EventSummary("baz",453))
  }


}


