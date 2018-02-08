package io.github.gitbucket.winbackup.actors

import java.io.{File, PrintWriter, StringWriter}

import akka.actor.{Actor, ActorRef, Props}
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.JDBCUtil.RichConnection
import io.github.gitbucket.winbackup.actors.DatabaseAccessActor.DumpDatabse
import io.github.gitbucket.winbackup.actors.MailActor.BackupFailure
import io.github.gitbucket.winbackup.actors.RepositoryCloneActor.Clone
import org.apache.commons.io.FileUtils

class DatabaseAccessActor(mailer: ActorRef) extends Actor with AccountService with RepositoryService {
  override def receive: Receive = {
    case DumpDatabse(baseDir) => {

      Database() withTransaction { implicit session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(baseDir, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)

        val repos = for {
          user <- getAllUsers()
          repo <- getRepositoryNamesOfUser(user.userName)
        } yield {
          Clone(baseDir, user.userName, repo)
        }
        sender() ! repos
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    reason.printStackTrace(pw)
    mailer ! BackupFailure(sw.toString)
  }
}

object DatabaseAccessActor {
  def props(mailer: ActorRef) = {
    Props[DatabaseAccessActor](new DatabaseAccessActor(mailer))
  }

  sealed case class DumpDatabse(baseDir: String)

}