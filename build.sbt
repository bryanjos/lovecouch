name := "lovecouch"

organization := "com.bryanjos"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "spray nightly repo" at "http://nightlies.spray.io"
)

libraryDependencies ++= Seq(
	"org.scalatest" % "scalatest_2.10" % "2.0.RC1-SNAP4",
	"com.typesafe.play" % "play-json_2.10" % "2.2.0-M1",
	"com.typesafe.akka" %% "akka-actor" % "2.2.1",
	"io.spray" % "spray-client" % "1.2-20131011"
)



initialCommands := "import com.bryanjos.lovecouch._"

testOptions in Test += Tests.Argument("-oD")
