import sbt._

object Dependencies {

  lazy val scramlDeps = Seq(
    "org.asynchttpclient"    %  "async-http-client" % "2.12.3", // http client by default used by scraml
    "com.typesafe.play"      %% "play-json"         % "2.9.4"  // play-json is used for json serialization
  )

  lazy val testDeps = Seq(
    "ch.qos.logback"         % "logback-classic"   % "1.4.5" % "test",
    "org.scalatest"          %% "scalatest"        % "3.2.15" % "test" withSources () withJavadoc (),
    "org.scalacheck"         %% "scalacheck"       % "1.17.0" % "test" withSources () withJavadoc (),
    "com.github.tomakehurst" % "wiremock"          % "2.27.2" % "test"
  )

}
