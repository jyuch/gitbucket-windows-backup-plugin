package io.github.gitbucket.winbackup.rx

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging
import gitbucket.core.util.{JGitUtil, Directory => gDirectory}
import io.github.gitbucket.winbackup.rx.RepositoryCloneActor.Clone
import io.github.gitbucket.winbackup.util.Directory
import org.apache.commons.io.FileUtils

class RepositoryCloneActor extends Actor {
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
}

object RepositoryCloneActor {
  def props = {
    Props[RepositoryCloneActor]
  }

  sealed case class Clone(baseDir: String, user: String, repo: String)

}