name := "gitbucket-windows-backup-plugin"
organization := "io.github.gitbucket"
version := "0.8.0"
scalaVersion := "2.12.6"
gitbucketVersion := "4.27.0"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.13",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.368"
)

scalacOptions ++= Seq("-deprecation", "-feature")