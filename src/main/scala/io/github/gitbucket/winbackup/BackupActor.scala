package io.github.gitbucket.winbackup

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.JDBCUtil.RichConnection
import gitbucket.core.util.{Directory, JGitUtil}
import io.github.gitbucket.winbackup.BackupActor.DoBackup
import org.apache.commons.io.FileUtils

class BackupActor extends Actor with AccountService with RepositoryService {

  private val logger = Logging(context.system, this)

  override def receive: Receive = {
    case _: DoBackup => {
      val startTimeMillis = System.currentTimeMillis()

      val backupDest = new File(Directory.GitBucketHome, "backup-" + System.currentTimeMillis())

      Database() withTransaction { implicit session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(backupDest, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)

        for (
          user <- getAllUsers();
          repo <- getRepositoryNamesOfUser(user.userName)
        ) {
          val src = Directory.getRepositoryDir(user.userName, repo)
          val dest = new File(new File(backupDest, user.userName), repo + ".git")
          JGitUtil.cloneRepository(src, dest)

          val wikiSrc = Directory.getWikiRepositoryDir(user.userName, repo)
          val wikiDest = new File(new File(backupDest, user.userName), repo + ".wiki.git")
          JGitUtil.cloneRepository(wikiSrc, wikiDest)
          logger.info(s"${user.userName} ${repo}")
        }
      }

      val endTimeMillis = System.currentTimeMillis()
      logger.info(s"Backup complete in ${endTimeMillis - startTimeMillis} ms")
    }
  }

}

object BackupActor {

  def props = {
    Props[BackupActor]
  }

  sealed case class DoBackup()

}