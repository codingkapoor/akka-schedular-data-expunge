package com.codingkapoor.dataexpunge

import com.codingkapoor.dataexpunge.core.`package`.Ttl

object `package` {

  val CASS = "cass"
  val SOLR = "solr"
  val VERTICA = "vertica"

  val supportedDbs = Set(CASS, SOLR, VERTICA)

  case class Do(ttl: Ttl)

  case object Done

  def getCustomerDbInfo(name: String): (String, String) = {
    val arr = name.split("_")
    val db = arr.tail.head
    val customer = arr.tail.tail.mkString("/")

    (db, customer)
  }
}
