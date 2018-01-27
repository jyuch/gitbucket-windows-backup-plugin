package io.github.gitbucket.winbackup.rx

import java.io.File

import akka.actor.{Actor, Props}
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.winbackup.rx.BackupActor.DoBackup
import io.github.gitbucket.winbackup.rx.DatabaseAccessActor.DumpDatabse
import io.github.gitbucket.winbackup.rx.FinishingActor.Finishing
import io.github.gitbucket.winbackup.rx.RepositoryCloneActor.Clone
import io.github.gitbucket.winbackup.util.Directory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class BackupActor(zipDest: Option[String], maxZip: Option[Int]) extends Actor with AccountService with RepositoryService {

  private val db = context.actorOf(DatabaseAccessActor.props, "db")
  private val cloner = context.actorOf(RepositoryCloneActor.props, "cloner")
  private val packer = context.actorOf(FinishingActor.props(zipDest, maxZip), "packer")

  override def receive: Receive = {
    case _: DoBackup => {
      val backupName = Directory.getBackupName

      val tempBackupDir = new File(gDirectory.GitBucketHome, backupName)
      implicit val timeout: Timeout = Timeout(5 minutes)

      val repos = (db ? DumpDatabse(tempBackupDir.getAbsolutePath)).mapTo[List[Clone]]

      repos foreach { r =>
        val c = r.map(cloner ? _)

        Future.sequence(c) foreach { _ =>
          packer ! Finishing(tempBackupDir.getAbsolutePath, backupName)
        }
      }
    }
  }
}

object BackupActor {

  def props(zipDest: Option[String], maxZip: Option[Int]) = {
    Props[BackupActor](new BackupActor(zipDest, maxZip))
  }

  sealed case class DoBackup()

}