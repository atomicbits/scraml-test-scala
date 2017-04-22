import sbt._

object Dependencies {

  lazy val scramlDeps = Seq(
    "com.ning"               % "async-http-client" % "1.9.40", // http client by default used by scraml
    "com.typesafe.play"      %% "play-json"        % "2.5.14"  // play-json is used for json serialization
  )

  lazy val testDeps = Seq(
    "ch.qos.logback"         % "logback-classic"   % "1.1.1" % "test",
    "org.scalatest"          %% "scalatest"        % "2.2.1" % "test" withSources () withJavadoc (),
    "org.scalacheck"         %% "scalacheck"       % "1.12.1" % "test" withSources () withJavadoc (),
    "com.github.tomakehurst" % "wiremock"          % "1.56" % "test"
  )



}