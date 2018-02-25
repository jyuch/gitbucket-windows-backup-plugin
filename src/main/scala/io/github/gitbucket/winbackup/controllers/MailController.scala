package io.github.gitbucket.winbackup.controllers

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.AdminAuthenticator
import io.github.gitbucket.winbackup.service.ActorService
import org.scalatra.Ok

class MailController extends ControllerBase with AdminAuthenticator with ActorService {

  post("/api/v3/winback/mailtest") {
    adminOnly {
      sendTestMail()
      Ok()
    }
  }

  post("/api/v3/winback/execute-backup") {
    adminOnly {
      executeBackup()
      Ok()
    }
  }

  override def shutdown(): Unit = {
    super[ControllerBase].shutdown()
  }

}
