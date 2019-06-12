package com.codingkapoor.dataexpunge.core

import akka.actor.{ActorRef, Props}
import com.codingkapoor.dataexpunge.`package`.{CASS, SOLR, VERTICA}
import com.codingkapoor.dataexpunge.cass.CassPartitionExpunge
import com.codingkapoor.dataexpunge.solr.SolrCollectionExpunge
import com.codingkapoor.dataexpunge.vertica.VerticaRecordExpunge

object `package` {
  type Ttl = Int
  type Freq = Int

  case class DeConfigsPerDb(ttl: Ttl, freq: Freq = 86400)
  type DeConfigsPerCustomer = Map[String, DeConfigsPerDb]
  type DeConfigs = Map[String, DeConfigsPerCustomer]

  type DataExpungeActorRef = ActorRef
  type SchedulerActorRef = ActorRef

  def confPath: String = RuntimeEnvironment.getConfDir + "/application.conf"

  val dataExpungeActorSysName = "data-expunge"
  val supervisorActorName = "supervisor"

  def getSupervisorPerCustomerActorName(customer: String): String =
    s"supervisor_$customer"

  def getSchedulerActorName(customer: String, db: String): String =
    s"scheduler_${db}_$customer"

  def getDataExpungeActorName(customer: String, db: String): String =
    s"dataexpunge_${db}_$customer"

  def getDataExpungeProps(db: String): Props = {
    db match {
      case CASS => CassPartitionExpunge.props
      case SOLR => SolrCollectionExpunge.props
      case VERTICA => VerticaRecordExpunge.props
    }
  }

}
