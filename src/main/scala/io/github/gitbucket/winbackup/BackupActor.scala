package io.github.gitbucket.winbackup

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.servlet.Database
import gitbucket.core.util.Directory
import gitbucket.core.util.JDBCUtil.RichConnection
import io.github.gitbucket.winbackup.BackupActor.DoBackup
import org.apache.commons.io.FileUtils

class BackupActor extends Actor {

  private val logger = Logging(context.system, this)

  override def receive: Receive = {
    case _: DoBackup => {
      logger.info("Do backup")

      val backupDest = new File(Directory.GitBucketHome, "backup-" + System.currentTimeMillis())

      Database() withTransaction { session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(backupDest, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)
      }
    }
  }

}

object BackupActor {

  def props = {
    Props[BackupActor]
  }

  sealed case class DoBackup()

}