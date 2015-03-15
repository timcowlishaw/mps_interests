name := "MPs interests"

version := "0.1"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.xerial" % "sqlite-jdbc" % "3.8.7",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M4",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0-M4",
  "de.l3s.boilerpipe" % "boilerpipe" % "1.1.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "xerces" % "xerces" % "2.4.0",
  "net.sourceforge.nekohtml" % "nekohtml" % "1.9.17",
  "cc.mallet" % "mallet" % "2.0.7"

)
