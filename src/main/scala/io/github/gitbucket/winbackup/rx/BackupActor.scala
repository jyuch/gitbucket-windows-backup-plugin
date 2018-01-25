package io.github.gitbucket.winbackup.rx

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.JDBCUtil.RichConnection
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.winbackup.rx.BackupActor.DoBackup
import io.github.gitbucket.winbackup.rx.RepositoryCloneActor.Clone
import io.github.gitbucket.winbackup.util.Directory
import org.apache.commons.io.FileUtils
import org.zeroturnaround.zip.ZipUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class BackupActor(zipDest: Option[String]) extends Actor with AccountService with RepositoryService {

  private val logger = Logging(context.system, this)
  private val cloner = context.actorOf(RepositoryCloneActor.props)

  override def receive: Receive = {
    case _: DoBackup => {
      val backupName = Directory.getBackupName

      val tempBackupDir = new File(gDirectory.GitBucketHome, backupName)

      Database() withTransaction { implicit session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(tempBackupDir, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)

        implicit val timeout: Timeout = Timeout(1 minutes)

        val repos = (for {
          user <- getAllUsers()
          repo <- getRepositoryNamesOfUser(user.userName)
        } yield {
          val src = gDirectory.getRepositoryDir(user.userName, repo)
          val dest = new File(new File(tempBackupDir, user.userName), repo + ".git")

          val wikiSrc = gDirectory.getWikiRepositoryDir(user.userName, repo)
          val wikiDest = new File(new File(tempBackupDir, user.userName), repo + ".wiki.git")

          List(Clone(src.getAbsolutePath, dest.getAbsolutePath), Clone(wikiSrc.getAbsolutePath, wikiDest.getAbsolutePath))
        }).flatten.map(cloner ? _)

        Future.sequence(repos) foreach { _ =>
          val zip = new File(zipDest.getOrElse(gDirectory.GitBucketHome), s"${backupName}.zip")
          ZipUtil.pack(tempBackupDir, zip)
          FileUtils.deleteDirectory(tempBackupDir)
          logger.info("Backup complete")
        }
      }
    }
  }
}

object BackupActor {

  def props(zipDest: Option[String]) = {
    Props[BackupActor](new BackupActor(zipDest))
  }

  sealed case class DoBackup()

}