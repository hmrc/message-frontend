import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, scalaSettings }

val appName = "message-frontend"

Global / majorVersion := 7
Global / scalaVersion := "3.3.6"

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[?]] = Seq.empty

lazy val TemplateTest = config("tt") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    (Seq(play.sbt.PlayScala, SbtDistributablesPlugin, SbtWeb) ++ plugins) *
  )
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(playSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    libraryDependencies ++= AppDependencies.appDependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator,
    scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    )
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.crypto._",
      "binders.PathBinders._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    )
  )
  .settings(inConfig(TemplateTest)(Defaults.testSettings) *)

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala, ScalafmtPlugin)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(libraryDependencies ++= AppDependencies.appDependencies)

Test / test := (Test / test)
  .dependsOn(scalafmtCheckAll)
  .value

it / test := (it / Test / test)
  .dependsOn(scalafmtCheckAll, it / scalafmtCheckAll)
  .value
