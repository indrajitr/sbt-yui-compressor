sbtPlugin := true

organization := "org.scala_tools.sbt"

name := "sbt-yui-compressor"

version := "0.1-SNAPSHOT"

licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalacOptions := Seq("-deprecation")

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.6"

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishTo <<= (version) { version =>
  val snapshot = "Nexus Repository for Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val release  = "Nexus Repository for Releases"  at "http://nexus.scala-tools.org/content/repositories/releases/"
  if (version endsWith "-SNAPSHOT") Some(snapshot) else Some(release)
}
