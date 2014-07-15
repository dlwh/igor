organization := "edu.berkeley.nlp.cs"

name := "igor"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>http://jacobandreas.net/</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:jacobandreas/igor.git</url>
    <connection>scm:git:git@github.com:jacobandreas/igor.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jacobandreas</id>
      <name>Jacob Andreas</name>
      <url>http://cs.berkeley.edu/~jda</url>
    </developer>
  </developers>

scalacOptions ++= Seq("-deprecation","-language:_")

javacOptions ++= Seq("-target", "1.6", "-source","1.6")

libraryDependencies ++= Seq(
   "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
   "org.scalanlp" %% "breeze" % "0.8.1",
   "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
   "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0-beta9",
   "org.apache.logging.log4j" % "log4j-core" % "2.0-beta9",
   "org.apache.logging.log4j" % "log4j-api" % "2.0-beta9",
   "org.json4s" %% "json4s-jackson" % "3.2.6",
   "org.yaml" % "snakeyaml" % "1.13"
)

// see https://github.com/typesafehub/scalalogging/issues/23
testOptions in Test += Tests.Setup(classLoader =>
  classLoader
    .loadClass("org.slf4j.LoggerFactory")
    .getMethod("getLogger", classLoader.loadClass("java.lang.String"))
    .invoke(null, "ROOT")
)

resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("public"),
    Resolver.typesafeRepo("releases")
)

testOptions in Test += Tests.Argument("-oDF")
