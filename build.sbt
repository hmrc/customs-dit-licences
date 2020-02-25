import AppDependencies._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt.{Resolver, _}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.PublishingSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.gitstamp.GitStampPlugin._

import scala.language.postfixOps

name := "customs-dit-licences"

targetJvm := "jvm-1.8"

lazy val allResolvers = resolvers ++= Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.jcenterRepo
)

lazy val AcceptanceTest = config("acceptance") extend Test
lazy val CdsIntegrationTest = config("it") extend Test

val testConfig = Seq(AcceptanceTest, CdsIntegrationTest, Test)

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq(testAll := (test in AcceptanceTest)
  .dependsOn((test in CdsIntegrationTest).dependsOn(test in Test)).value)

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .enablePlugins(SbtDistributablesPlugin)
  .enablePlugins(SbtArtifactory)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .configs(testConfig: _*)
  .settings(
    commonSettings,
    unitTestSettings,
    integrationTestSettings,
    acceptanceTestSettings,
    playPublishingSettings,
    allTest,
    scoverageSettings,
    allResolvers
  )
  .settings(majorVersion := 0)

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(onPackageName("unit"))),
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(CdsIntegrationTest)(Defaults.testTasks) ++
    Seq(
      testOptions in CdsIntegrationTest := Seq(Tests.Filters(Seq(onPackageName("integration"), onPackageName("acceptance")))),
      fork in CdsIntegrationTest := false,
      parallelExecution in CdsIntegrationTest := false,
      addTestReportOption(CdsIntegrationTest, "int-test-reports"),
      testGrouping in CdsIntegrationTest := forkedJvmPerTestConfig((definedTests in Test).value, "integration", "acceptance")
    )

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testTasks) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(onPackageName("acceptance"))),
      fork in AcceptanceTest := false,
      parallelExecution in AcceptanceTest := false,
      addTestReportOption(AcceptanceTest, "acceptance-reports")
    )


lazy val commonSettings: Seq[Setting[_]] =
  scalaSettings ++
  publishingSettings ++
  defaultSettings() ++
  gitStampSettings

lazy val playPublishingSettings: Seq[sbt.Setting[_]] =  Seq(credentials += SbtCredentials) ++
  publishAllArtefacts

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "uk\\.gov\\.hmrc\\.customs\\.dit\\.licence\\.services\\.ConfigService",
    ".*(Reverse|BuildInfo|Routes).*").mkString(";"),
  coverageMinimum := 98,
  coverageFailOnMinimum := false,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

val compileDependencies = Seq(customsApiCommon)

val testDependencies = Seq(hmrcTest, scalaTestPlusPlay, wireMock, mockito, customsApiCommonTests)

libraryDependencies ++= compileDependencies ++ testDependencies

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)
