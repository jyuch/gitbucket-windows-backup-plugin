package io.github.gitbucket.winbackup.util

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import gitbucket.core.util.{Directory => gDirectory}

object Directory {
  val BackupConf = new File(gDirectory.GitBucketHome, "backup.conf")

  def getBackupName: String = {
    val now = LocalDateTime.now
    val f = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    s"backup-${f.format(now)}"
  }
}
