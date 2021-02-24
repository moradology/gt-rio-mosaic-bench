organization := "com.azavea"

name := "mosaictest"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.12"

resolvers += "locationtech-releases" at "https://repo.eclipse.org/content/groups/releases"
resolvers += "jts-snapshots" at "https://repo.eclipse.org/content/repositories/jts-snapshots"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-raster" % "3.5.3-SNAPSHOT",
  "org.locationtech.geotrellis" %% "geotrellis-vector" % "3.5.3-SNAPSHOT",
  "org.locationtech.geotrellis" %% "geotrellis-layer" % "3.5.3-SNAPSHOT",
  "org.http4s" %% "http4s-blaze-client" % "0.21.7",
  "org.http4s" %% "http4s-blaze-server" % "0.21.7",
  "org.http4s" %% "http4s-dsl" % "0.21.7",
  "org.http4s" %% "http4s-circe" % "0.21.7",
)

initialCommands in console := """
"""

assemblyMergeStrategy in assembly := {
  case "reference.conf"                       => MergeStrategy.concat
  case "application.conf"                     => MergeStrategy.concat
  case n if n.startsWith("META-INF/services") => MergeStrategy.concat
  case n
      if n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA") || n
        .endsWith(".semanticdb") =>
    MergeStrategy.discard
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case _                      => MergeStrategy.first
}

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
