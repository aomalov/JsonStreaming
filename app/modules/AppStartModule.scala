package modules

import java.time.Clock

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.AbstractModule
import flow.{JsonInputStream, JsonInputStreamStdioImpl, JsonInputStreamTestImpl}
import services._



/**
 ** Run some startup routines
 */
class AppStartModule extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[JsonInputStream]).to(classOf[JsonInputStreamStdioImpl])
    bind(classOf[AppStarter]).asEagerSingleton()
  }

}
