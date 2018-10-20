import Tests._

enablePlugins(GitVersioning, S3Plugin, GitBranchPrompt, GitPlugin)
git.useGitDescribe := true

val playJsonVersion = "2.7.0-M1"

lazy val defaultSettings = Seq(
  name := "jobnik",
  organization := "com.liorregev",
  javaOptions ++= Seq("-Xms512M", "-Xmx8192M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),

  scalacOptions ++= Seq(
    "-feature", "-deprecation", "-unchecked", "-explaintypes",
    "-encoding", "UTF-8", // yes, this is 2 args
    "-language:reflectiveCalls", "-language:implicitConversions", "-language:postfixOps", "-language:existentials",
    "-language:higherKinds",
    // http://blog.threatstack.com/useful-scala-compiler-options-part-3-linting
    "-Xcheckinit", "-Xexperimental", "-Xfatal-warnings", /*"-Xlog-implicits", */"-Xfuture", "-Xlint",
    "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-numeric-widen", "-Yno-adapted-args", "-Ywarn-unused-import",
    "-Ywarn-unused"
  ),

  wartremoverErrors ++= Seq(
    Wart.StringPlusAny, Wart.FinalCaseClass, Wart.JavaConversions, Wart.Null, Wart.Product, Wart.Serializable,
    Wart.LeakingSealed, Wart.While, Wart.Return, Wart.ExplicitImplicitTypes, Wart.Enumeration, Wart.FinalVal,
    Wart.TryPartial, Wart.TraversableOps, Wart.OptionPartial, ContribWart.SomeApply
  ),

  wartremoverWarnings ++= wartremover.Warts.allBut(
    Wart.Nothing, Wart.DefaultArguments, Wart.Throw, Wart.MutableDataStructures, Wart.NonUnitStatements, Wart.Overloading,
    Wart.Option2Iterable, Wart.ImplicitConversion, Wart.ImplicitParameter, Wart.Recursion,
    Wart.Any, Wart.Equals, // Too many warnings because of spark's Row
    Wart.AsInstanceOf, // Too many warnings because of bad DI practices
    Wart.ArrayEquals // Too many warnings because we're using byte arrays in Spark
  ),

  testFrameworks := Seq(TestFrameworks.ScalaTest),
  logBuffered in Test := false,

  scalaVersion := "2.12.7",

  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("public"),
    Resolver.typesafeRepo("releases")
  ),

  // This needs to be here for Coursier to be able to resolve the "tests" classifier, otherwise the classifier's ignored
  classpathTypes += "test-jar"
)

lazy val assemblySettings = Seq(
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
  test in assembly := {},

  assemblyMergeStrategy in assembly := {
    case x if x.endsWith("application.conf") => MergeStrategy.first
    case x if x.endsWith(".class") => MergeStrategy.last
    case x if x.endsWith("logback.xml") => MergeStrategy.first
    case x if x.endsWith(".properties") => MergeStrategy.last
    case x if x.contains("/resources/") => MergeStrategy.last
    case x if x.startsWith("META-INF/mailcap") => MergeStrategy.last
    case x if x.startsWith("META-INF/mimetypes.default") => MergeStrategy.first
    case x if x.startsWith("META-INF/maven/org.slf4j/slf4j-api/pom.") => MergeStrategy.first
    case x if x.startsWith("CHANGELOG.") => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      if (oldStrategy == MergeStrategy.deduplicate)
        MergeStrategy.first
      else
        oldStrategy(x)
  }
)

lazy val `serialization` = project.in(file("libraries/serialization"))
  .settings(defaultSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.joda"                    % "joda-convert"    % "1.9.2",
      "com.typesafe.play"          %% "play-json"       % playJsonVersion,
      "com.typesafe.play"          %% "play-json-joda"  % playJsonVersion,
      "org.scalatest"              %% "scalatest"       % "3.0.4"           % "test"
    )
  )

lazy val jobnik = project.in(file("jobnik"))
  .dependsOn(serialization)
  .settings(defaultSettings ++ assemblySettings)
  .settings(
    // Allow parallel execution of tests as long as each of them gets its own JVM to create a SparkContext on (see SPARK-2243)
    fork in Test := true,
    testGrouping in Test := (definedTests in Test)
      .value
      .map(test => Group(test.name, Seq(test), SubProcess(ForkOptions()))),

    testOptions in Test := Seq()
  )
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream"         % "2.5.17",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.17",
    "com.typesafe.play" %% "play-json"           % playJsonVersion,
    "org.scalatest"     %% "scalatest"           % "3.0.4" % Test
  ))

lazy val publishJar = taskKey[Seq[String]]("Deploy fat JAR to S3")
lazy val incrementVersion = taskKey[Unit]("Creates git tags if needed on master")

lazy val root = project.in(file("."))
  .settings(defaultSettings)
  .aggregate(jobnik, `serialization`)
