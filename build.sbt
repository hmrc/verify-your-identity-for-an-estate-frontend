import scoverage.ScoverageKeys

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 0

lazy val appName: String = "verify-your-identity-for-an-estate-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 8831,
    ScoverageKeys.coverageExcludedFiles := ".*Routes.*;.*handlers.*;.*components.*;.*Routes.*;.*Mode.*;.*Page.*",
    ScoverageKeys.coverageMinimumStmtTotal := 81,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=views/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    pipelineStages := Seq(digest)
  )

addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
