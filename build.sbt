name := "gitbucket-windows-backup-plugin"
organization := "io.github.gitbucket"
version := "1.0.0"
scalaVersion := "2.12.4"
gitbucketVersion := "4.21.0"

libraryDependencies += "org.zeroturnaround" % "zt-zip" % "1.12"

scalacOptions ++= Seq("-deprecation", "-feature")