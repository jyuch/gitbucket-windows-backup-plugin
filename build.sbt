name := "gitbucket-windows-backup-plugin"
organization := "io.github.gitbucket"
version := "0.6.0"
scalaVersion := "2.12.5"
gitbucketVersion := "4.23.1"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.12",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.277"
)

scalacOptions ++= Seq("-deprecation", "-feature")