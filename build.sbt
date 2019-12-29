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
    // https://mvnrepository.com/artifact/org.typelevel/cats-core
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.0",
    // https://mvnrepository.com/artifact/org.typelevel/cats-effect
    libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0",
    // https://mvnrepository.com/artifact/dev.zio/zio
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17",
    // https://mvnrepository.com/artifact/dev.zio/zio-streams
    libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC17",
    // https://mvnrepository.com/artifact/dev.zio/zio-interop-cats
    libraryDependencies += "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10",
    // https://mvnrepository.com/artifact/co.fs2/fs2-core
    libraryDependencies += "co.fs2" %% "fs2-core" % "2.1.0",
    // https://mvnrepository.com/artifact/co.fs2/fs2-io
    libraryDependencies += "co.fs2" %% "fs2-io" % "2.1.0",
    // https://mvnrepository.com/artifact/org.scodec/scodec-core
    libraryDependencies += "org.scodec" %% "scodec-core" % "1.11.4",
      // https://mvnrepository.com/artifact/org.scodec/scodec-bits
    libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.12",
    // https://mvnrepository.com/artifact/com.typesafe/config
    libraryDependencies += "com.typesafe" % "config" % "1.4.0",
    // https://mvnrepository.com/artifact/io.circe/circe-config
    libraryDependencies += "io.circe" %% "circe-config" % "0.7.0",
    // https://mvnrepository.com/artifact/io.circe/circe-generic
    libraryDependencies += "io.circe" %% "circe-generic" % "0.12.3"
  )