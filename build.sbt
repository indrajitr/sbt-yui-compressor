sbtPlugin := true

organization := "in.drajit.sbt"

name := "sbt-yui-compressor"

version := "0.2-SNAPSHOT"

licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalacOptions := Seq("-deprecation")

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.6"

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishTo <<= (isSnapshot) { isSnapshot =>
  val (namePrefix, repoBase) = ("sbt-plugin-", "http://scalasbt.artifactoryonline.com/scalasbt/")
  val (name, repo) = if (isSnapshot) (namePrefix + "snapshots", repoBase + namePrefix + "snapshot")
                     else            (namePrefix + "releases", repoBase + namePrefix + "releases")
  Some(Resolver.url(name, url(repo))(Resolver.ivyStylePatterns))
}
