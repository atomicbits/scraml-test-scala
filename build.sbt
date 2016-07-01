name := "scraml-test-scala"

organization := "io.atomicbits"

version := "0.4.15-SNAPSHOT"

scalaVersion := "2.11.8"


// Sonatype snapshot resolver is needed to fetch SNAPSHOT releases of scraml
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.9.36", // http client by default used by scraml
  "io.atomicbits" %% "scraml-dsl-scala" % scramlVersion.value, // scraml DSL dependency
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test" withSources() withJavadoc(),
  "com.github.tomakehurst" % "wiremock" % "1.56" % "test"
)

scramlRamlApi in scraml in Compile := "io/atomicbits/scraml/TestClient01.raml"

scramlLicenseKey in scraml in Compile :=
  """
    |U1Q7Q1ZQWnZMbHZSakQ7TGVwN0JSYkJXT2I7aW8uYXRvbWljYml0czsyMDE2LTA3LTAxOzM2NiFj
    |VzlubUlqM2t4a1o2aUZFUFhDQm5nb01pd005bXVrb0k3MVczUnI2cmxaYjc3UC92OWlRUDFXeHBD
    |Mk9Gd2hQcHBUVXkwYVBQZFhRCnJ6Y3VialdQV2d1c3gxVHVaOTAxNEVKbmI4cm9vdk1FakVqNlZw
    |UG1mLzVyaHdTUXg5bWRtOWxtTDdUVnJoWTM1MEZaNVNJVWxGcG4Kb201cWVkdGp5cEs5dHRhajd3
    |V3FEby9jN2FJOEtNZk9lME5wWjhidDBzUFFoZkdvV2VDMEZCWU9pTU1BdkZPWlNuWTZDNk1UNmhM
    |WQo0eEoxVWFrTkNaaFNOeHhqRUQyNk44SHBFcFlybjlKWWRBa1piME5mVm9hczM0bHQ4WmN1OWJE
    |bFExK3FmaDlmazlpbm5SdjVRVkhrCnEyMFJCZ3NKbHlubnFlZHdJYm5oNTk3V1hFTE4yVzNyTC9l
    |Mjl3PT0=
  """.stripMargin

scramlClassHeader in scraml in Compile :=
  """
    |All rights reserved.
    |This is a custom license header.
  """.stripMargin

// Publish settings

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    None // Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := <url>https://github.com/atomicbits/scraml-test-scala</url>
  <licenses>
    <license>
      <name>AGPL license</name>
      <url>http://www.gnu.org/licenses/agpl-3.0.en.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:atomicbits/scraml-test-scala.git</url>
    <connection>scm:git:git@github.com:atomicbits/scraml-test-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rigolepe</id>
      <name>Peter Rigole</name>
      <url>http://atomicbits.io</url>
    </developer>
  </developers>

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield
  Seq(Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password)
  )).getOrElse(Seq())

