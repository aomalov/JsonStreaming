package flow

import akka.NotUsed
import akka.stream.scaladsl.Source
import javax.inject.Singleton

import scala.io.{Source => IOSource, StdIn}

trait JsonInputStream {
  def source:Source[String,NotUsed]
}

@Singleton
class JsonInputStreamFastTestImpl extends JsonInputStream {
  override def source: Source[String, NotUsed] = {
    val someLines=IOSource.fromResource("samples/json-stream-real.txt").getLines().toList
    Source.cycle[String](()=>someLines.iterator)
  }
}

@Singleton
class JsonInputStreamStdioImpl extends JsonInputStream {
  override def source: Source[String, NotUsed] = {
    Source.fromIterator(()=>Iterator.continually(StdIn.readLine()).takeWhile(_!=null))
  }
}
