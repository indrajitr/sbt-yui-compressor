resolvers += Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

libraryDependencies <+= (sbtVersion) { sv =>
  sv.split('.') match {
    case Array("0", "11", "2") => "org.scala-tools.sbt" %% "scripted-plugin" % sv
    case Array("0", "11", _)   => "org.scala-sbt"       %% "scripted-plugin" % sv
    case _                     => "org.scala-sbt"        % "scripted-plugin" % sv
  }
}
