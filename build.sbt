name := "lovecouch"

organization := "com.bryanjos"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
	"net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
	"com.cloudphysics" % "jerkson_2.10" % "0.6.3"
)



initialCommands := "import com.bryanjos.lovecouch._"

