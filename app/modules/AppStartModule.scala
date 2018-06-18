package modules

import java.time.Clock

import com.google.inject.AbstractModule
import flow.{JsonInputStream, JsonInputStreamFastTestImpl, JsonInputStreamStdioImpl}
import services._



/**
 ** Run some startup routines
 */
class AppStartModule extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
//    bind(classOf[JsonInputStream]).to(classOf[JsonInputStreamStdioImpl])
    bind(classOf[JsonInputStream]).to(classOf[JsonInputStreamFastTestImpl])
    bind(classOf[AppStarter]).asEagerSingleton()
  }

}
