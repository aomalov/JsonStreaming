package services

import akka.pattern._
import akka.util.Timeout
import flow.{EventStatsRequest, JsonInputStream, JsonInputStreamFastTestImpl}
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._

import scala.concurrent.duration._


class TestFastStreamer extends PlaySpec with GuiceOneAppPerSuite with WordSpecLike with ScalaFutures {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(Map("app.design.fast-producer" -> true))
      .overrides(bind[JsonInputStream].to[JsonInputStreamFastTestImpl])
      .build()
  }

  implicit val timeout: Timeout = 1.second
  //    implicit val pc=PatienceConfiguration()

  private val akkaManagement = app.injector.instanceOf[AkkaManagement]

  "App with fast producer" should {
    "get running event stats from Archiver Actor" in {
      var firstMeasurement:Long=0
      var secondMeasurement:Long=0

      Thread.sleep(2000) //Wait before we can conflate some data
      whenReady(akkaManagement.getArchiverActor.ask(EventStatsRequest)) {
        case res: JsValue =>
          firstMeasurement = (res \ "bar").as[Long]
          firstMeasurement must(be > 0L)
        case _ =>
      }
      Thread.sleep(2000) //Wait before we can conflate some more data
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
