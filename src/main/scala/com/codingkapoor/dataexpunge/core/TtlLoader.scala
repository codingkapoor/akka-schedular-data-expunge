package com.codingkapoor.dataexpunge.core

import java.nio.file.Paths
import com.codingkapoor.dataexpunge._
import pureconfig.ConfigReader.Result
import pureconfig.generic.auto._

import scala.util.control.NonFatal

trait TtlLoader extends Logger {
  private final val EMPTY: DeConfigs = Map()

  def isValid(deConfigs: DeConfigs): Boolean = {
    val dbs = deConfigs.values.flatMap(_.keySet).toSet

    val unsupportedDbs = dbs diff supportedDbs
    if (unsupportedDbs.nonEmpty)
      logger.error(s"Data expunge for dbs = ${unsupportedDbs.mkString(",")} is not supported. Please fix data expunge configurations.")

    unsupportedDbs.isEmpty
  }

  def fetchDeConfigs(): DeConfigs = {
    try {
      // To test: pureconfig.loadConfig[Map[String, TtlPerDatabase]](ConfigFactory.parseString("""ttl: { "vce/vce/pod": {cass:80, solr:90, vertica:10}, "ibm/ibm/pod": {cass:21, solr:7, vertica:3} }"""), "scalar.ttl")
      val deConfigs: Result[DeConfigs] = pureconfig.loadConfig[DeConfigs](Paths.get(confPath), "deconf")
      deConfigs match {
        case Left(configReaderFailures) =>
          logger.error(s"Encountered the following errors reading data expunge configurations: ${configReaderFailures.toList.mkString("\n")}.", false)
          EMPTY
        case Right(config) =>
          if (isValid(config)) config
          else EMPTY
      }
    } catch {
      case NonFatal(e) =>
        logger.error(s"Encountered the following errors reading data expunge configurations:")
        e.printStackTrace()
        EMPTY
    }
  }
}
