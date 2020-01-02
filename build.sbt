name := "xox"

version := "0.1"

scalaVersion := "2.13.1"

lazy val core = (project in file("core"))
  .settings(
    // https://mvnrepository.com/artifact/org.scodec/scodec-core
    libraryDependencies += "org.scodec" %% "scodec-core" % "1.11.4"
  )

lazy val server = (project in file("server"))
  .dependsOn(core)
  .settings(
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.1",
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
    libraryDependencies += "io.circe" %% "circe-generic" % "0.12.3"
  )