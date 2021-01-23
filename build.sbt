import sbt.Keys.{javaOptions, libraryDependencies, resolvers}

ThisBuild / organization := "ch.japanimpact"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JDebPackaging, SystemdPlugin, JavaServerAppPackaging)
  .settings(
    name := "ji-accreds",
    libraryDependencies ++= Seq(jdbc, evolutions, ehcache, ws, specs2 % Test, guice),

    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.4",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1",
    libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.8.1",
    libraryDependencies += "ch.japanimpact" %% "jiauthframework" % "2.0-SNAPSHOT",
    libraryDependencies += "ch.japanimpact" %% "ji-events-api" % "1.0-SNAPSHOT",
    libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34",
    libraryDependencies += "com.pauldijou" %% "jwt-play" % "4.2.0",

    maintainer in Linux := "Louis Vialar <louis.vialar@japan-impact.ch>",
    packageSummary in Linux := "Scala Backend for Japan Impact Accreds Platform",
    packageDescription := "Scala Backend for Japan Impact Accreds Platform",
    debianPackageDependencies := Seq("java8-runtime-headless"),

    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/",
    resolvers += "Japan Impact Snapshot Repository" at "https://repository.japan-impact.ch/snapshots/",
    resolvers += Resolver.mavenCentral,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation"
    ),

    javaOptions in Universal ++= Seq(
      // Provide the PID file
      s"-Dpidfile.path=/dev/null",
      // s"-Dpidfile.path=/run/${packageName.value}/play.pid",

      // Set the configuration to the production file
      s"-Dconfig.file=/usr/share/${packageName.value}/conf/production.conf",

      // Apply DB evolutions automatically
      "-DapplyEvolutions.default=true"
    ),
  )

