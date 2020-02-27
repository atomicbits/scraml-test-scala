import sbt._
import sbt.Keys._

object BuildSettings {

  val Organization = "io.atomicbits"

  val snapshotSuffix = "-SNAPSHOT"

  val Version = "0.8.0" + snapshotSuffix

  // val ScalaVersion = "2.12.10"
  val ScalaVersion = "2.13.1"

  val scalacBuildOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xlint:-infer-any",
    "-encoding",
    "UTF-8",
    "-target:jvm-1.8",
    "-Ybackend:GenBCode",
    "-Ydelambdafy:method"
  )

  def projSettings(dependencies: Seq[ModuleID]) = {
    projectSettings(dependencies) ++ publishSettings
  }

  def projectSettings(extraDependencies: Seq[ModuleID] = Seq()): Seq[sbt.Def.Setting[_]] = Seq(
    organization := Organization,
    version := Version,
    isSnapshot := Version.endsWith(snapshotSuffix),
    scalaVersion := ScalaVersion,
    parallelExecution := false,
    testForkedParallel := false,
    libraryDependencies ++= extraDependencies
  )

  val publishSettings = Seq(
    publishMavenStyle := true,
    pomIncludeRepository := { _ =>
      false
    },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        None // Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= publishingCredentials,
    pomExtra := pomInfo
  )

  lazy val publishingCredentials = (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password))).getOrElse(Seq())


  lazy val pomInfo = <url>https://github.com/atomicbits/scraml-test-scala</url>
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

  lazy val defaultSettings = Seq(
    scalacOptions := scalacBuildOptions
  )

}
