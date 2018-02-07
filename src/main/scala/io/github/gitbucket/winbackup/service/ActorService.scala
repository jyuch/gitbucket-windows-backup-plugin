package io.github.gitbucket.winbackup.service

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import io.github.gitbucket.winbackup.actors.BackupActor
import io.github.gitbucket.winbackup.util.Directory

trait ActorService {

  import ActorService._

  def initialize() = {
    scheduler.schedule("Backup", system.actorOf(BackupActor.props, "backup"), BackupActor.DoBackup())
  }

  def shutdown() = {
    system.terminate()
  }

}

object ActorService {
  private val config = ConfigFactory.parseFile(Directory.BackupConf)
  private val system = ActorSystem("winbackup", config)
  private val scheduler = QuartzSchedulerExtension(system)
}