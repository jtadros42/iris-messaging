lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion     = "2.5.25"
lazy val commonsVersions = "1.4"
lazy val playVersion     = "2.7.0"
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.8"
    )),
    name := "iris_messaging",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster"         % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools"   % akkaVersion,
      "commons-cli"       % "commons-cli"           % commonsVersions,
      "com.typesafe.play" %% "play-json"            % playVersion,
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,
      "org.scalamock"     %% "scalamock"            % "4.4.0"         % Test
    )
  )
