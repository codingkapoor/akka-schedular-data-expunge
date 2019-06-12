package com.codingkapoor.dataexpunge.core

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.util.Timeout
import com.codingkapoor.dataexpunge.core.Scheduler.NewDeConfigsPerDb

import scala.concurrent.duration.FiniteDuration

object SupervisorPerCustomer {

  case class NewDeConfigsPerCustomer(mps: String, ttls: DeConfigsPerCustomer)

  def props: Props = Props[SupervisorPerCustomer]

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  def modifiedDeConfigsPerCustomer(lastDeConfigsPerCustomer: DeConfigsPerCustomer, deConfigsPerCustomer: DeConfigsPerCustomer): Set[String] = {
    val common = lastDeConfigsPerCustomer.keySet intersect deConfigsPerCustomer.keySet
    common.filter { l =>
      lastDeConfigsPerCustomer(l) != deConfigsPerCustomer(l)
    }
  }
}

class SupervisorPerCustomer extends Actor with ActorLogging {

  import SupervisorPerCustomer._

  log.info(s"Created ${this.self.path.name}.")

  var lastDeConfigsPerCustomer: DeConfigsPerCustomer = Map()
  var childrens: Map[String, (SchedulerActorRef, DataExpungeActorRef)] = Map()

  private def createSchedulerAndDeActors(mps: String, deConfigsPerCustomer: DeConfigsPerCustomer, db: String): (SchedulerActorRef, DataExpungeActorRef) = {
    val dataExpunge: DataExpungeActorRef = context.actorOf(getDataExpungeProps(db), getDataExpungeActorName(mps, db))
    val scheduler: SchedulerActorRef = context.actorOf(Scheduler.props, getSchedulerActorName(mps, db))

    scheduler ! (NewDeConfigsPerDb(deConfigsPerCustomer(db)), dataExpunge)

    (scheduler, dataExpunge)
  }

  override def receive: Receive = {
    case NewDeConfigsPerCustomer(mps, deConfigsPerCustomer) =>
      val dbs = deConfigsPerCustomer.keySet

      val added = dbs diff lastDeConfigsPerCustomer.keySet
      val removed = lastDeConfigsPerCustomer.keySet diff dbs
      val modified = modifiedDeConfigsPerCustomer(lastDeConfigsPerCustomer, deConfigsPerCustomer)

      log.debug(s"lastDeConfigsPerCustomer = $lastDeConfigsPerCustomer, added = $added, removed = $removed, modified = $modified")

      added.foreach { db =>
        childrens += db -> createSchedulerAndDeActors(mps, deConfigsPerCustomer, db)

        log.info(s"Added db = $db.")
        log.debug(s"childrens = $childrens")
      }

      removed.foreach { db =>
        val (scheduler, dataExpunge) = childrens(db)

        scheduler ! PoisonPill
        dataExpunge ! PoisonPill

        childrens -= db

        log.info(s"Removed db = $db.")
        log.debug(s"childrens = $childrens")
      }

      modified.foreach { db =>
        val (scheduler, _) = childrens(db)
        scheduler ! NewDeConfigsPerDb(deConfigsPerCustomer(db))

        log.info(s"Modified db = $db.")
        log.debug(s"childrens = $childrens")
      }

      lastDeConfigsPerCustomer = deConfigsPerCustomer

    case r =>
      log.warning(s"${this.self.path.name}, Message = $r not recognized.")
  }
}
