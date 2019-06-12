package com.codingkapoor.dataexpunge.core

import akka.actor.{ActorRef, ActorSystem}
import com.codingkapoor.dataexpunge.core.Supervisor.NewDeConfigs

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DataExpunge extends App with TtlWatcher {

  implicit def actorSystem: ActorSystem = ActorSystem(dataExpungeActorSysName)

  private val _supervisor: ActorRef = actorSystem.actorOf(Supervisor.props, supervisorActorName)

  def supervisor: ActorRef = _supervisor

  val ttls = fetchDeConfigs()
  if (ttls.nonEmpty) supervisor ! NewDeConfigs(ttls)

  Await.result(actorSystem.whenTerminated, Duration.Inf)
}
