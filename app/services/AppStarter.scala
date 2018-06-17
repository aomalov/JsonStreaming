package services

import java.time.{Clock, Instant}

import akka.actor.{ActorSystem, PoisonPill}
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future


@Singleton
class AppStarter @Inject()(clock: Clock,
                           appLifecycle: ApplicationLifecycle,
                           akkaManagement: AkkaManagement,
                           actorSystem: ActorSystem,
                           jStreamer: JStreamer) {

  // This code is called when the application starts.
  private val start: Instant = clock.instant
  Logger.info(s"ApplicationTimer demo: Starting application at $start.")
  jStreamer.startStream()

  //Shutdown hook
  appLifecycle.addStopHook { () =>
    val stop: Instant = clock.instant
    val runningTime: Long = stop.getEpochSecond - start.getEpochSecond
    Logger.info(s"ApplicationTimer demo: Stopping application at ${clock.instant} after ${runningTime}s.")
    akkaManagement.getShutdownSwitch.shutdown()
    akkaManagement.getArchiverActor ! PoisonPill
//    actorSystem.whenTerminated
    Future.successful(())
  }
}
