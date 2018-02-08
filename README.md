gitbucket-windows-backup-plugin
===
Provides repository backup for GitBucket on Windows

## Features
This plugin provides backup feature for below data.

- Database contents
- User repositories
- Wiki repositories
- Attachment file of issue and release
- User avatar data 

And email notification what backup success or failure.

## Configuration
Configuration `GITBUCKET_HOME/backup.conf` as below.

```
# Backup timing (Required)
# For details, see http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
# This example, backup 12am every day
akka {
  quartz {
    schedules {
      Backup {
        expression = "0 0 0 * * ?"
      }
    }
  }
}

winbackup {
  # Backup archive destination directory (Optional)
  # If not specified, archive is saved into GITBUCKET_HOME
  archive-destination = """C:\archive-dest-dir"""

  # Maximum number of backup archives to keep (if 0 or negative value, keep unlimited) (Optional)
  # If not specified, keep unlimited
  archive-limit = 10

  # Send notify email when backup is success (Optional, default:false)
  notify-on-success = true

  # Send notify email when backup is failure (Optional, default:false)
  notify-on-failure = true

  # Notify email destination (Optional)
  notify-dest = ["jyuch@localhost"]
}
```

And you need to setup gitbucket SMTP configuration when use email notification.

## Send test mail

Send HTTP Post to `http://localhost:8080/api/v3/winback/mailtest` when you want to send test mail.

## Compatibility with GitBucket

|Plugin version|GitBucket version|
|:-:|:-:|
|0.3.0|4.21|
|0.2.0|4.20|
|0.1.0|4.20|
