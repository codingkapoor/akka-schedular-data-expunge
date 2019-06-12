package com.codingkapoor.dataexpunge.vertica

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.codingkapoor.dataexpunge.`package`.getCustomerDbInfo
import com.codingkapoor.dataexpunge.core.Logger
import com.codingkapoor.dataexpunge.core.`package`.Ttl
import com.codingkapoor.dataexpunge.{Do, Done}

object VerticaRecordExpunge {
  def props: Props = Props[VerticaRecordExpunge]
}

class VerticaRecordExpunge extends Actor with Logger {

  logger.info(s"Created ${this.self.path.name}.")

  lazy val actorName: String = this.self.path.name
  lazy val (db, customer) = getCustomerDbInfo(actorName)
  var last: LocalDateTime = LocalDateTime.now()

  override def receive: Receive = {
    case Do(ttl) =>
      val current = LocalDateTime.now()
      logger.debug(s"customer = $customer, db = $db, event = $Do($ttl), lastDo = $last, currentDo = $current")
      last = current

      // expunge(ttl)

      sender ! Done
  }

  private def expunge(ttl: Ttl): Unit = ???

}
