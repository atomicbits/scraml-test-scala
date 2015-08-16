addSbtPlugin("io.atomicbits"      % "scraml-sbt-plugin"   % "0.1.0-SNAPSHOT")

// Sonatype snapshot resolver is needed to fetch rxhttpclient-scala_2.11:0.2.0-SNAPSHOT.
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
