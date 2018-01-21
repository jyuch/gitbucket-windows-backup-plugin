package io.github.gitbucket.winbackup

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging
import gitbucket.core.util.JGitUtil
import io.github.gitbucket.winbackup.RepositoryCloneActor.Clone

class RepositoryCloneActor extends Actor {
  private[this] val logger = Logging(context.system, this)

  override def receive: Receive = {
    case Clone(src, dest) => {
      JGitUtil.cloneRepository(new File(src), new File(dest))
      logger.info("Clone repository {} -> {}", src, dest)
      sender() ! ()
    }
  }
}

object RepositoryCloneActor {
  def props = {
    Props[RepositoryCloneActor]
  }

  sealed case class Clone(src: String, dest: String)

}