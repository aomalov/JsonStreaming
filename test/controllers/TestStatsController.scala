package controllers

import flow.{JsonInputStream, JsonInputStreamFastTestImpl}
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._


class TestStatsController extends PlaySpec with GuiceOneAppPerSuite with WordSpecLike with ScalaFutures with PatienceConfiguration {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(Map("app.design.fast-producer" -> true))
      .overrides(bind[JsonInputStream].to[JsonInputStreamFastTestImpl])
      .build()
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout =  Span(2, Seconds), interval = Span(1500, Milliseconds))

  "Simple controller action" should {
    "Return a JSON body" in {
      Thread.sleep(2000) //Wait before we can conflate some data

      val result=route(app,FakeRequest("GET", "/words")).get
      status(result) must be(200)
      contentAsString(result).contains("lorem") must be(true)
    }
  }

}
