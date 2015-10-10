name := "scraml-test-scala"

organization := "io.atomicbits"

version := "0.3.4-SNAPSHOT"

scalaVersion := "2.11.5"

// Sonatype snapshot resolver is needed to fetch SNAPSHOT releases of scraml
 resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test" withSources() withJavadoc(),
  "com.github.tomakehurst" % "wiremock" % "1.56" % "test"
)

scramlRamlApi in scraml in Compile := "io/atomicbits/scraml/TestClient01.raml"
