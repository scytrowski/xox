name := "xox"

version := "0.1"

scalaVersion := "2.13.1"

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")

lazy val core = (project in file("core"))
  .settings(
    // https://mvnrepository.com/artifact/com.beachape/enumeratum
    libraryDependencies += "com.beachape" %% "enumeratum" % "1.5.15",
    // https://mvnrepository.com/artifact/org.scodec/scodec-core
    libraryDependencies += "org.scodec" %% "scodec-core" % "1.11.4"
  )

lazy val server = (project in file("server"))
  .dependsOn(core)
  .settings(
    scalacOptions += "-Ypartial-unification"
  )
  .settings(
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-stream
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.3",
    // https://mvnrepository.com/artifact/org.typelevel/cats-core
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.0",
    // https://mvnrepository.com/artifact/org.scodec/scodec-core
    libraryDependencies += "org.scodec" %% "scodec-core" % "1.11.4",
    // https://mvnrepository.com/artifact/org.scodec/scodec-bits
    libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.12",
    // https://mvnrepository.com/artifact/org.scodec/scodec-stream
    libraryDependencies += "org.scodec" %% "scodec-stream" % "2.0.0",
    // https://mvnrepository.com/artifact/com.typesafe/config
    libraryDependencies += "com.typesafe" % "config" % "1.4.0",
    // https://mvnrepository.com/artifact/io.circe/circe-config
    libraryDependencies += "io.circe" %% "circe-config" % "0.7.0",
    // https://mvnrepository.com/artifact/io.circe/circe-generic
    libraryDependencies += "io.circe" %% "circe-generic" % "0.12.3",
    // https://mvnrepository.com/artifact/org.scalatest/scalatest
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-M2" % Test,
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.3" % Test,
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-stream-testkit
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.3" % Test
  )
  .settings(
    Docker / name := "xox-server",
    Docker / packageName := "xox-server",
    Docker / defaultLinuxInstallLocation := "/opt/app",
    Docker / defaultLinuxLogsLocation := "/opt/app/logs"
  )
  .enablePlugins(JavaAppPackaging)

lazy val api = (project in file("api"))
  .dependsOn(core)
  .settings(
    scalacOptions += "-Ypartial-unification"
  )
