import javax.servlet.ServletContext

import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService
import io.github.gitbucket.solidbase.model.Version
import io.github.gitbucket.winbackup.controllers.MailController
import io.github.gitbucket.winbackup.service.ActorService
import org.slf4j.LoggerFactory

class Plugin extends gitbucket.core.plugin.Plugin with ActorService {

  override val pluginId: String = "winbackup"
  override val pluginName: String = "Backup Plugin for Windows"
  override val description: String = "Provides data backup for GitBucket on Windows"
  override val versions: List[Version] = List(new Version("1.0.0"))

  private val logger = LoggerFactory.getLogger(classOf[Plugin])

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    super.initialize(registry, context, settings)
    initialize()
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    shutdown()
    logger.info("{} is shutting down.", pluginName)
  }

  override val controllers = Seq(
    "/*" -> new MailController
  )
}
