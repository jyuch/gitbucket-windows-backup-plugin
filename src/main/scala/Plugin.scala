import java.io.File
import java.nio.charset.StandardCharsets
import javax.servlet.ServletContext

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService
import gitbucket.core.util.Directory
import io.github.gitbucket.solidbase.model.Version
import io.github.gitbucket.winbackup.BackupActor
import org.slf4j.LoggerFactory

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "winbackup"
  override val pluginName: String = "Backup Plugin for Windows"
  override val description: String = "Provides data backup for GitBucket on Windows"
  override val versions: List[Version] = List(new Version("1.0.0"))

  private val logger = LoggerFactory.getLogger(classOf[Plugin])

  private val system = ActorSystem("winbackup", ConfigFactory.parseString(
    org.apache.commons.io.FileUtils.readFileToString(
      new File(Directory.GitBucketHome, "backup.conf"), StandardCharsets.UTF_8)))

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    super.initialize(registry, context, settings)

    val scheduler = QuartzSchedulerExtension(system)
    scheduler.schedule("Backup", system.actorOf(BackupActor.props, "backup"), BackupActor.DoBackup())
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    system.terminate()
    logger.info("Backup is shutting down.")
  }
}
