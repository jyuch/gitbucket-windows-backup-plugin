gitbucket-windows-backup-plugin
===
Provides data backup for GitBucket on Windows

## Configuration
Configuration `GITBUCKET_HOME/backup.conf` as below.

```
# (Required) backup 12am every day
# For details, see http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
akka.quartz.schedules.Backup.expression = "0 0 0 * * ?"

# (Optional) zip archive destination or GITBUCKET_HOME
zipDest = """C:\zip-dest"""
```

## Backup target

- database content
- user repository
- wiki repository