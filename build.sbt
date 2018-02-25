name := "gitbucket-windows-backup-plugin"
organization := "io.github.gitbucket"
version := "0.4.0"
scalaVersion := "2.12.4"
gitbucketVersion := "4.21.0"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.12",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.277"
)

scalacOptions ++= Seq("-deprecation", "-feature")