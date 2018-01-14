package io.github.gitbucket.winbackup

import akka.actor.{Actor, Props}
import akka.event.Logging
import io.github.gitbucket.winbackup.BackupActor.DoBackup

class BackupActor extends Actor {

  private val logger = Logging(context.system, this)

  override def receive: Receive = {
    case _: DoBackup => {
      logger.info("Do backup")
    }
  }

}

object BackupActor {

  def props = {
    Props[BackupActor]
  }

  sealed case class DoBackup()

}