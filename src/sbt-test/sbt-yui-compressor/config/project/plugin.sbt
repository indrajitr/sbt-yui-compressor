resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("in.drajit.sbt" % "sbt-yui-compressor" % "0.2.2-SNAPSHOT")
