package com.codingkapoor.dataexpunge.core

import scala.sys.SystemProperties

object RuntimeEnvironment {
  private lazy val sp = new SystemProperties()

  def getConfDir: String = {
    val vmParam = "conf.dir"
    val r = sp(vmParam)

    if (r != null && !r.trim.isEmpty) r.trim else throw new RuntimeException(s"-D$vmParam was not provided.")
  }
}
