val enumeratumCirceVersion  = "1.5.13"
val kantanCsvVersion        = "0.5.1"
val http4sVersion           = "0.20.6"
val circeVersion            = "0.11.1"

val commonSettings = Seq(
  scalaVersion        :=  "2.12.8",
  scalacOptions       ++= Seq(
    "-Ypartial-unification",
    "-Ywarn-unused:implicits",
    "-Xfatal-warnings",
    "-feature",
  ),
  libraryDependencies +=  "com.lihaoyi" %% "utest" % "0.7.1" % "test",
  testFrameworks      += new TestFramework("utest.runner.Framework"),
  
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    name                :=  "tickets4sale-core",
    version             :=  "0.1",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"             % "1.6.1",
      "org.typelevel" %% "cats-effect"           % "1.3.1",

      "com.beachape"  %% "enumeratum"            % "1.5.13",

      "com.nrinaudo"  %% "kantan.csv-java8"      % kantanCsvVersion,
      "com.nrinaudo"  %% "kantan.csv-generic"    % kantanCsvVersion,
    )
  )

lazy val cli  = (project in file("cli"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name                :=  "tickets4sale-cli",
    version             :=  "0.1",
    libraryDependencies ++= Seq(
      "com.monovore"  %% "decline"               % "0.5.0",
      "io.circe"      %% "circe-generic"         % circeVersion,
      "com.beachape"  %% "enumeratum"            % enumeratumCirceVersion,
    )
  )

lazy val api = (project in file("api"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name                :=  "tickets4sale-api",
    version             :=  "0.1",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-dsl"            % http4sVersion,
      "org.http4s"    %% "http4s-blaze-server"   % http4sVersion,
      "org.http4s"    %% "http4s-circe"          % http4sVersion,
      "io.circe"      %% "circe-generic"         % circeVersion,
      "com.beachape"  %% "enumeratum"            % enumeratumCirceVersion,
    )
  )
