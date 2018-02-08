package io.github.gitbucket.winbackup.actors

import java.io.{File, PrintWriter, StringWriter}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.winbackup.actors.FinishingActor.Finishing
import io.github.gitbucket.winbackup.actors.MailActor.{BackupFailure, BackupSuccess}
import io.github.gitbucket.winbackup.service.PluginSettingsService
import io.github.gitbucket.winbackup.util.Directory
import org.apache.commons.io.FileUtils
import org.zeroturnaround.zip.ZipUtil

class FinishingActor(mailer: ActorRef) extends Actor with ActorLogging with PluginSettingsService {

  private val config = loadPluginSettings()

  override def receive: Receive = {
    case Finishing(baseDir, backupName) => {
      val tempBackupDir = new File(baseDir)

      val data = Directory.getDataBackupDir(tempBackupDir)
      FileUtils.copyDirectory(new File(gDirectory.DatabaseHome), data)

      val zip = new File(config.archiveDestination.getOrElse(gDirectory.GitBucketHome), s"${backupName}.zip")
      ZipUtil.pack(tempBackupDir, zip)
      FileUtils.deleteDirectory(tempBackupDir)

      config.archiveLimit foreach { n =>
        if (n > 0) {
          val pattern = """^backup-\d{12}\.zip$""".r
          val d = new File(config.archiveDestination.getOrElse(gDirectory.GitBucketHome))

          val t = d.listFiles.filter(
            _.getName match {
              case pattern() => true
              case _ => false
            }).sortBy(_.getName).reverse.drop(n)

          t.foreach({ f =>
            log.info("Delete archive {}", f.getAbsoluteFile)
            f.delete()
          })
        }
      }

      log.info("Backup complete")
      mailer ! BackupSuccess()
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    reason.printStackTrace(pw)
    mailer ! BackupFailure(sw.toString)
  }
}

object FinishingActor {
  def props(mailer: ActorRef) = {
    Props[FinishingActor](new FinishingActor(mailer))
  }

  sealed case class Finishing(baseDir: String, backupName: String)

}