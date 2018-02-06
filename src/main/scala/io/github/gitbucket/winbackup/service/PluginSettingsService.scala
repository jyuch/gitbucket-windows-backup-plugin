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
      config.getOptionalInt("winbackup.archive-limit")
    )
  }
}

object PluginSettingsService {

  case class PluginSettings(archiveDestination: Option[String],
                            archiveLimit: Option[Int])

}