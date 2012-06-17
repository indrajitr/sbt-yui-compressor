sbtPlugin := true

organization := "in.drajit.sbt"

name := "sbt-yui-compressor"

version := "0.2.0-SNAPSHOT"

licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalacOptions ++= DefaultOptions.scalac :+ Opts.compile.deprecation

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.6"

ScriptedPlugin.scriptedSettings

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishMavenStyle := false

publishTo <<= (isSnapshot) { iss =>
  val (namePrefix, repoBase) = ("sbt-plugin-", "http://scalasbt.artifactoryonline.com/scalasbt/")
  val resolver = if (iss) (namePrefix + "snapshots", repoBase + namePrefix + "snapshots")
                 else     (namePrefix + "releases", repoBase + namePrefix + "releases")
  Some(Resolver.url(resolver._1, url(resolver._2))(Resolver.ivyStylePatterns))
}
