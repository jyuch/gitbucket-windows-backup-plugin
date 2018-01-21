package io.github.gitbucket.winbackup

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.Directory
import gitbucket.core.util.JDBCUtil.RichConnection
import io.github.gitbucket.winbackup.BackupActor.DoBackup
import io.github.gitbucket.winbackup.RepositoryCloneActor.Clone
import org.apache.commons.io.FileUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class BackupActor extends Actor with AccountService with RepositoryService {

  private val logger = Logging(context.system, this)
  private val cloner = context.actorOf(RepositoryCloneActor.props)

  override def receive: Receive = {
    case _: DoBackup => {
      val now = LocalDateTime.now
      val f = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")

      val backupDest = new File(Directory.GitBucketHome, "backup-" + f.format(now))

      Database() withTransaction { implicit session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(backupDest, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)

        implicit val timeout: Timeout = Timeout(5 seconds)

        val repos = (for {
          user <- getAllUsers()
          repo <- getRepositoryNamesOfUser(user.userName)
        } yield {
          val src = Directory.getRepositoryDir(user.userName, repo)
          val dest = new File(new File(backupDest, user.userName), repo + ".git")

          val wikiSrc = Directory.getWikiRepositoryDir(user.userName, repo)
          val wikiDest = new File(new File(backupDest, user.userName), repo + ".wiki.git")

          List(Clone(src.getAbsolutePath, dest.getAbsolutePath), Clone(wikiSrc.getAbsolutePath, wikiDest.getAbsolutePath))
        }).flatten.map(cloner ? _)

        Future.sequence(repos) foreach {
          case _ => logger.info("Backup complete")
        }
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