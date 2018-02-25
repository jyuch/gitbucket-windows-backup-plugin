package io.github.gitbucket.winbackup.service

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import io.github.gitbucket.winbackup.actors.BackupActor
import io.github.gitbucket.winbackup.actors.BackupActor.{DoBackup, SendTestMail}
import io.github.gitbucket.winbackup.util.Directory

trait ActorService {

  import ActorService._

  def initialize(): Unit = {
    scheduler.schedule("Backup", backupActor, BackupActor.DoBackup())
  }

  def shutdown(): Unit = {
    system.terminate()
  }

  def sendTestMail(): Unit = {
    backupActor ! SendTestMail()
  }

  def executeBackup(): Unit = {
    backupActor ! DoBackup()
  }

}

object ActorService {
  private val config = ConfigFactory.parseFile(Directory.BackupConf)
  private val system = ActorSystem("winbackup", config)
  private val scheduler = QuartzSchedulerExtension(system)
  private val backupActor = system.actorOf(BackupActor.props, "backup")
}