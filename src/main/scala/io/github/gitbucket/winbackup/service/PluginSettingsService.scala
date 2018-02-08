package io.github.gitbucket.winbackup.service

import com.typesafe.config.ConfigFactory
import io.github.gitbucket.winbackup.service.PluginSettingsService.PluginSettings

trait PluginSettingsService {

  import io.github.gitbucket.winbackup.util.ConfigHelper._
  import io.github.gitbucket.winbackup.util.Directory

  def loadPluginSettings(): PluginSettings = {
    val config = ConfigFactory.parseFile(Directory.BackupConf)
    PluginSettings(
      config.getOptionalString("winbackup.archive-destination"),
      config.getOptionalInt("winbackup.archive-limit"),
      config.getOptionalBoolean("winbackup.notify-on-success").getOrElse(false),
      config.getOptionalBoolean("winbackup.notify-on-failure").getOrElse(false),
      config.getOptionalStringList("winbackup.notify-dest")
    )
  }
}

object PluginSettingsService {

  case class PluginSettings(archiveDestination: Option[String],
                            archiveLimit: Option[Int],
                            notifyOnSuccess: Boolean,
                            notifyOnFailure: Boolean,
                            notifyDestination: Option[List[String]])


}