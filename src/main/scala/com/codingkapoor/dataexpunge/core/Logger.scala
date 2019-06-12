package com.codingkapoor.dataexpunge.core

import com.typesafe.scalalogging.{Logger => ScalaLogger}
import org.slf4j.LoggerFactory

trait Logger {
  private def getLogger[T](myClass: T): ScalaLogger = {
    ScalaLogger(LoggerFactory.getLogger(myClass.getClass))
  }

  protected final lazy val logger = getLogger(this)
}
