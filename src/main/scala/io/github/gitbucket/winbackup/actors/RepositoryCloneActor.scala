package io.github.gitbucket.winbackup.actors

import java.io.{File, PrintWriter, StringWriter}

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import gitbucket.core.util.{JGitUtil, Directory => gDirectory}
import io.github.gitbucket.winbackup.actors.MailActor.BackupFailure
import io.github.gitbucket.winbackup.actors.RepositoryCloneActor.Clone
import io.github.gitbucket.winbackup.util.Directory
import org.apache.commons.io.FileUtils

class RepositoryCloneActor(mailer: ActorRef) extends Actor {
  private[this] val logger = Logging(context.system, this)

  override def receive: Receive = {
    case Clone(baseDir, user, repo) => {
      val src = gDirectory.getRepositoryDir(user, repo)
      val dest = Directory.getRepositoryBackupDir(new File(baseDir), user, repo)
      JGitUtil.cloneRepository(src, dest)

      val wikiSrc = gDirectory.getWikiRepositoryDir(user, repo)
      val wikiDest = Directory.getWikiBackupDir(new File(baseDir), user, repo)
      JGitUtil.cloneRepository(wikiSrc, wikiDest)

      val filesSrc = gDirectory.getRepositoryFilesDir(user, repo)
      val filesDest = Directory.getRepositoryFilesBackupDir(new File(baseDir), user, repo)
      if (filesSrc.exists() && filesSrc.isDirectory) {
        FileUtils.copyDirectory(filesSrc, filesDest)
      }

      logger.info("Clone repository {}/{}", user, repo)
      sender() ! ((): Unit)
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

object RepositoryCloneActor {
  def props(mailer: ActorRef) = {
    Props[RepositoryCloneActor](new RepositoryCloneActor(mailer))
  }

  sealed case class Clone(baseDir: String, user: String, repo: String)

}