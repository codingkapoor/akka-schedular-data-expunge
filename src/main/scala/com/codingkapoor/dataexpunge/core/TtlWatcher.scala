package com.codingkapoor.dataexpunge.core

import akka.actor.{ActorRef, ActorSystem}
import better.files.File
import better.files.FileWatcher._
import java.nio.file.{StandardWatchEventKinds => EventType}

import com.codingkapoor.dataexpunge.core.Supervisor.NewDeConfigs

trait TtlWatcher extends TtlLoader with Logger {

  implicit def actorSystem: ActorSystem

  def supervisor: ActorRef

  private val appConfFile = File(confPath)
  private var appConfLastModified = appConfFile.lastModifiedTime

  val watcher: ActorRef = appConfFile.newWatcher(recursive = false)

  logger.info(s"Created TtlWatcher: ${watcher.path.name}.")

  watcher ! on(EventType.ENTRY_MODIFY) { file =>
    if (appConfLastModified.compareTo(file.lastModifiedTime) < 0) {

      val deConfigs = fetchDeConfigs()
      if (deConfigs.nonEmpty) supervisor ! NewDeConfigs(deConfigs)

      appConfLastModified = file.lastModifiedTime
    }
  }

}
