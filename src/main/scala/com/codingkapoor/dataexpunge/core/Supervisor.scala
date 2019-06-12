package com.codingkapoor.dataexpunge.core

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import com.codingkapoor.dataexpunge.core.SupervisorPerCustomer.NewDeConfigsPerCustomer

import scala.concurrent.duration.FiniteDuration

object Supervisor {

  case class NewDeConfigs(deConfigs: DeConfigs)

  def props: Props = Props[Supervisor]

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  def modifiedDeConfigs(lastDeConfigs: DeConfigs, deConfigs: DeConfigs): Set[String] = {
    val common = lastDeConfigs.keySet intersect deConfigs.keySet
    common.filter { l =>
      lastDeConfigs(l) != deConfigs(l)
    }
  }
}

class Supervisor extends Actor with Logger {
  import Supervisor._

  logger.info(s"Created ${this.self.path.name}.")

  var lastDeConfigs: DeConfigs = Map()
  var childrens: Map[String, ActorRef] = Map()

  override def receive: Receive = {
    case NewDeConfigs(deConfigs) =>
      val customers = deConfigs.keySet

      val added = customers diff lastDeConfigs.keySet
      val removed = lastDeConfigs.keySet diff customers
      val modified = modifiedDeConfigs(lastDeConfigs, deConfigs)

      logger.debug(s"lastDeConfigs = $lastDeConfigs, added = $added, removed = $removed, modified = $modified")

      added.foreach { customer =>
        val actorRef = context.actorOf(SupervisorPerCustomer.props, getSupervisorPerCustomerActorName(customer))
        actorRef ! NewDeConfigsPerCustomer(customer, deConfigs(customer))

        childrens += customer -> actorRef

        logger.info(s"Added customer = $customer.")
        logger.debug(s"childrens = $childrens")
      }

      removed.foreach { customer =>
        childrens(customer) ! PoisonPill
        childrens -= customer

        logger.info(s"Removed customer = $customer.")
        logger.debug(s"childrens = $childrens")
      }

      modified.foreach { customer =>
        childrens(customer) ! NewDeConfigsPerCustomer(customer, deConfigs(customer))

        logger.info(s"Modified mps = $customer.")
        logger.debug(s"childrens = $childrens")
      }

      lastDeConfigs = deConfigs

    case r =>
      logger.warn(s"${this.self.path.name}, Message: $r not recognized.")
  }
}
