name := "lovecouch"

organization := "com.bryanjos"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
"spray" at "http://repo.spray.io/",
"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.10" % "2.0.RC1-SNAP4",
	"net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
	"com.typesafe.play" % "play-json_2.10" % "2.2.0-M1",
	"com.twitter" % "finagle-http_2.10" % "6.5.2"
)



initialCommands := "import com.bryanjos.lovecouch._"

