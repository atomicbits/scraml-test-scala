name := "scraml-test-scala"

organization := "io.atomicbits"

version := "0.4.1-SNAPSHOT"

scalaVersion := "2.11.5"

// Sonatype snapshot resolver is needed to fetch SNAPSHOT releases of scraml
 resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.1" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test" withSources() withJavadoc(),
  "com.github.tomakehurst" % "wiremock" % "1.56" % "test"
)

scramlRamlApi in scraml in Compile := "io/atomicbits/scraml/TestClient01.raml"


// Publish settings

publishMavenStyle := true

pomIncludeRepository := { _ => false}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=  <url>https://github.com/atomicbits/scraml-test-scala</url>
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

