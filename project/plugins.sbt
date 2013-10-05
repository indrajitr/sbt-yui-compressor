addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

libraryDependencies <+= (sbtVersion) { sv =>
  sv.split('.') match {
    case Array("0", "11", "2") => "org.scala-tools.sbt" %% "scripted-plugin" % sv
    case Array("0", "11", _)   => "org.scala-sbt"       %% "scripted-plugin" % sv
    case _                     => "org.scala-sbt"        % "scripted-plugin" % sv
  }
}
