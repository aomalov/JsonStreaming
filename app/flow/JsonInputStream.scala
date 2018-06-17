package flow

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.ImplementedBy
import javax.inject.Singleton

import scala.io.{Source => IOSource}

@ImplementedBy(classOf[JsonInputStreamImpl])
trait JsonInputStream {
  def source:Source[String,NotUsed]
}

@Singleton
class JsonInputStreamImpl extends JsonInputStream {
  override def source: Source[String, NotUsed] = Source.cycle[String](()=>IOSource.fromResource("json-stream-real.txt").getLines())
}
