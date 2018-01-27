package io.github.gitbucket.winbackup.rx

import java.io.File

import akka.actor.{Actor, ActorLogging, Props}
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.winbackup.rx.FinishingActor.Finishing
import io.github.gitbucket.winbackup.util.Directory
import org.apache.commons.io.FileUtils
import org.zeroturnaround.zip.ZipUtil

class FinishingActor(zipDest: Option[String]) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Finishing(baseDir, backupName) => {
      val tempBackupDir = new File(baseDir)

      val data = Directory.getDataBackupDir(tempBackupDir)
      FileUtils.copyDirectory(new File(gDirectory.DatabaseHome), data)

      val zip = new File(zipDest.getOrElse(gDirectory.GitBucketHome), s"${backupName}.zip")
      ZipUtil.pack(tempBackupDir, zip)
      FileUtils.deleteDirectory(tempBackupDir)
      log.info("Backup complete")
    }
  }
}

object FinishingActor {
  def props(zipDest: Option[String]) = {
    Props[FinishingActor](new FinishingActor(zipDest))
  }

  sealed case class Finishing(baseDir: String, backupName: String)

}