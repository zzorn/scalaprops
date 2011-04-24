import sbt._

class ScalaPropsProject(info: ProjectInfo) extends DefaultProject(info) {
  // Repositories
  val scalaTools = "Scala-Tools Maven2 Releses Repository" at "http://scala-tools.org/repo-releases"
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

  // Dependencies
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

  val migLayout = "com.miglayout" % "miglayout" % "3.7.4"  classifier "swing"

}
