import java.io.File
import javax.servlet.ServletContext

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.{Config, ConfigFactory}
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService
import gitbucket.core.util.Directory
import io.github.gitbucket.solidbase.model.Version
import io.github.gitbucket.winbackup.BackupActor
import org.slf4j.LoggerFactory

class Plugin extends gitbucket.core.plugin.Plugin {

  import ConfigHelper.RichConfig

  override val pluginId: String = "winbackup"
  override val pluginName: String = "Backup Plugin for Windows"
  override val description: String = "Provides data backup for GitBucket on Windows"
  override val versions: List[Version] = List(new Version("1.0.0"))

  private val logger = LoggerFactory.getLogger(classOf[Plugin])

  private val config = ConfigFactory.parseFile(new File(Directory.GitBucketHome, "backup.conf"))

  private val system = ActorSystem("winbackup", config)

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    super.initialize(registry, context, settings)

    val scheduler = QuartzSchedulerExtension(system)
    val zipDest = config.getOptionalString("zipDest")
    scheduler.schedule("Backup", system.actorOf(BackupActor.props(zipDest), "backup"), BackupActor.DoBackup())
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    system.terminate()
    logger.info("Backup is shutting down.")
  }
}

object ConfigHelper {

  implicit class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }
  }

}
