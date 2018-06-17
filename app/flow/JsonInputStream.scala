package flow

import akka.NotUsed
import akka.stream.scaladsl.Source
import javax.inject.Singleton

import scala.io.{Source => IOSource, StdIn}

trait JsonInputStream {
  def source:Source[String,NotUsed]
}

@Singleton
class JsonInputStreamTestImpl extends JsonInputStream {
  override def source: Source[String, NotUsed] = Source.cycle[String](()=>IOSource.fromResource("samples/json-stream-real.txt").getLines())
}

@Singleton
class JsonInputStreamStdioImpl extends JsonInputStream {
  override def source: Source[String, NotUsed] = {
//    Source.fromIterator(()=>IOSource.fromInputStream(System.in).getLines())
    Source.fromIterator(()=>Iterator.continually(StdIn.readLine()).takeWhile(_!=null))
  }
}
