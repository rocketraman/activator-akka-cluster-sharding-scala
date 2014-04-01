import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.3.0"

val project = Project(
  id = "akka-cluster-sharding-scala",
  base = file("."),
  settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "akka-cluster-sharding-scala",
    version := "1.0",
    scalaVersion := "2.10.3",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "2.0" % "test",
      "commons-io" % "commons-io" % "2.4" % "test",
      "org.apache.logging.log4j" % "log4j-api" % "2.0-rc1",
      "org.apache.logging.log4j" % "log4j-core" % "2.0-rc1",
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0-rc1",
      "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "0.0.8"),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiNodeResults.events,
            testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)
