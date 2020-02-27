import sbt._

object Dependencies {

  lazy val scramlDeps = Seq(
    "com.ning"               % "async-http-client" % "1.9.40", // http client by default used by scraml
    "com.typesafe.play"      %% "play-json"        % "2.8.1"  // play-json is used for json serialization
  )

  lazy val testDeps = Seq(
    "ch.qos.logback"         % "logback-classic"   % "1.2.3" % "test",
    "org.scalatest"          %% "scalatest"        % "3.1.1" % "test" withSources () withJavadoc (),
    "org.scalacheck"         %% "scalacheck"       % "1.14.3" % "test" withSources () withJavadoc (),
    "com.github.tomakehurst" % "wiremock"          % "2.26.0" % "test"
  )

}
