package modules

import java.time.Clock

import com.google.inject.AbstractModule
import services._

/**
 ** Run some startup routines
 */
class AppStartModule extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bind(classOf[JStreamer]).to(classOf[JStreamerImpl])
  }

}
