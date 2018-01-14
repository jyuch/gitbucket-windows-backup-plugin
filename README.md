gitbucket-windows-backup-plugin
===
Provides data backup for GitBucket on Windows

Configuration `GITBUCKET_HOME/backup.conf` as below.

``` 
akka {
  quartz {
    schedules {
      Backup {
        expression = "*/5 * * ? * *"
      }
    }
  }
}
```