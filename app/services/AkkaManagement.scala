package services

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._
import com.google.inject.ImplementedBy
import flow.StatsArchiveActor
import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[AkkaManagementImpl])
trait AkkaManagement {
  def getShutdownSwitch: SharedKillSwitch
  def getArchiverActor: ActorRef
}


@Singleton
class AkkaManagementImpl @Inject() (actorSystem: ActorSystem) extends AkkaManagement {
  private val archiver = actorSystem.actorOf(Props[StatsArchiveActor])
  private val killerSwitch = KillSwitches.shared("akka-flow-stopper")

  override def getShutdownSwitch: SharedKillSwitch = killerSwitch

  override def getArchiverActor: ActorRef = archiver
}


