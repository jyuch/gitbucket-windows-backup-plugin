gitbucket-windows-backup-plugin
===
Provides data backup for GitBucket on Windows

## Features
This plugin provides backup feature for below data.

- Database contents
- User repositories
- Wiki repositories
- Attachment files of issue and release
- User avatar data 

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
}
```

## Compatibility with GitBucket

|Plugin version|GitBucket version|
|:-:|:-:|
|0.3.0|4.21|
|0.2.0|4.20|
|0.1.0|4.20|
