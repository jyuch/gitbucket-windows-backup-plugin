package io.github.gitbucket.winbackup.util

import com.typesafe.config.Config

object ConfigHelper {

  implicit class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }

    def getOptionalInt(path: String): Option[Int] = {
      if (underlying.hasPath(path)) {
        Some(underlying.getInt(path))
      } else {
        None
      }
    }
  }

}