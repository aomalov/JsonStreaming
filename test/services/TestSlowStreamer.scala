package services

import akka.NotUsed
import akka.pattern._
import akka.stream.scaladsl.Source
import akka.util.Timeout
import flow.{EventStatsRequest, JsonInputStream}
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._

import scala.concurrent.duration._

case object InputMock extends JsonInputStream {
  override def source: Source[String, NotUsed] = {
    Source.tick[String](5.milliseconds,200.milliseconds,"{ \"event_type\": \"bar\", \"data\": \"sit\", \"timestamp\": 1528997716 }")
      .mapMaterializedValue(_=>NotUsed)
  }
}


class TestSlowStreamer extends PlaySpec with GuiceOneAppPerSuite with WordSpecLike with ScalaFutures {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(Map("app.design.fast-producer" -> false))
      .overrides(bind[JsonInputStream].toInstance(InputMock))
      .build()
  }

  implicit val timeout: Timeout = 1.second
  //    implicit val pc=PatienceConfiguration()

  private val akkaManagement = app.injector.instanceOf[AkkaManagement]

  "App with slow producer" should {
    "get running event stats from Archiver Actor" in {
      var firstMeasurement:Long=0
      var secondMeasurement:Long=0

      Thread.sleep(500)
      whenReady(akkaManagement.getArchiverActor.ask(EventStatsRequest)) {
        case res: JsValue =>
          firstMeasurement = (res \ "bar").as[Long]
          firstMeasurement must(be > 0L)
        case _ =>
      }
      Thread.sleep(500)
      whenReady(akkaManagement.getArchiverActor.ask(EventStatsRequest)) {
        case res: JsValue =>
          secondMeasurement = (res \ "bar").as[Long]
          secondMeasurement must(be > 0L)
        case _ =>
      }
      secondMeasurement must(be >firstMeasurement)
    }
  }



}
