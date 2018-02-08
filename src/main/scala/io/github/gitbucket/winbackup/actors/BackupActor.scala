package io.github.gitbucket.winbackup.actors

import java.io.File

import akka.actor.{Actor, Props}
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.winbackup.actors.BackupActor.{DoBackup, SendTestMail}
import io.github.gitbucket.winbackup.actors.DatabaseAccessActor.DumpDatabse
import io.github.gitbucket.winbackup.actors.FinishingActor.Finishing
import io.github.gitbucket.winbackup.actors.MailActor.TestMail
import io.github.gitbucket.winbackup.actors.RepositoryCloneActor.Clone
import io.github.gitbucket.winbackup.util.Directory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class BackupActor extends Actor {

  private val mailer = context.actorOf(Props[MailActor], "mailer")
  private val db = context.actorOf(DatabaseAccessActor.props(mailer), "db")
  private val cloner = context.actorOf(RepositoryCloneActor.props(mailer), "cloner")
  private val packer = context.actorOf(FinishingActor.props(mailer), "packer")

  override def receive: Receive = {
    case DoBackup() => {
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
    case SendTestMail() => {
      mailer ! TestMail()
    }
  }
}

object BackupActor {

  def props() = {
    Props[BackupActor]
  }

  sealed case class DoBackup()

  sealed case class SendTestMail()

}