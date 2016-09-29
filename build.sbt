name := "scraml-test-scala"

organization := "io.atomicbits"

version := "0.5.0-SNAPSHOT"

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
    |U1Q7Q1ZQWnZMbHZSakQ7TGVwN0JSYkJXT2I7c29tZW9uZTsyMDE2LTA3LTIzOy0xIXNpMG9keE1k
    |M2FqeVpGb2paYWVJWEFlTWg5bWFMNS84UU84MVpMTkwwMlVvZlNhREM1a3RPVEZzckkzY09yR0VK
    |NmNnNXExZjdSaEEKNmNaMndDQlg0OGcyWDVVVkMwVWlFNjdDVmVWdXR6VnBNaTZxWVdGZ05YYXVM
    |ZlJPSDdCcEJCcjQ5VDBPU3FnVzlLYVQ5Rm5SUm5UbQpIcVVLQ2xXMnppSHZDd0lmVkR5OFk1MEE1
    |b1NEUTc5SWVDSlFPZUlVYTVkbWsrWTNJd04wZkpBZmIzZEhKKzhJTm1JZUR2aTY2QnBVClN0WFAw
    |UEFGSXJTdHlqSGtTaEpzOXNDbVBwQkZUWWpFY2tqb3pKME11VWJkVzRXWVpKVDhrbXR4c1AzZjNO
    |MXVuWWMxcW9WQlJjSnYKSWwvMjZqWDUzZURneXMvbFFOdEVYUjE1WjlyU2Y3dk00SW95V3c9PQ==
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

