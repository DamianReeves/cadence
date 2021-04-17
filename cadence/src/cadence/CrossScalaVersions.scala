package cadence

object CrossScalaVersions extends App {
  import scala.util.matching.Regex
  import java.nio.file._

  val ScalaVersionPattern: Regex = """([0-9]+)\.([0-9]+).*""".r

  def scalaVersionPaths(scalaVersion: String, f: String => Path, isDotty: Boolean = false) = {
    def resolvePaths(additional: List[String]) = {
      val common = for (segments <- scalaVersion.split('.').inits.filter(_.nonEmpty)) yield segments.mkString(".")
      val paths  = common.toSet ++ additional.toSet
      paths.map(f)
    }

    partialVersion(scalaVersion) match {
      case Some((2, 11)) =>
        resolvePaths(List("2.11", "2.11+", "2.11-2.12", "2.x"))
      case Some((2, 12)) =>
        resolvePaths(List("2.12", "2.11+", "2.12+", "2.11-2.12", "2.12-2.13", "2.x"))
      case Some((2, 13)) =>
        resolvePaths(List("2.13", "2.11+", "2.12+", "2.13+", "2.12-2.13", "2.x"))
      case _ if isDotty =>
        resolvePaths(List("dotty", "2.11+", "2.12+", "2.13+", "3.x"))
      case _ =>
        resolvePaths(List())
    }
  }

  def partialVersion(version: String) = version match {
    case ScalaVersionPattern(major, minor) => Option(major.toInt -> minor.toInt)
    case _                                 => None
  }

  val scalaVersions = List("2.11.12", "2.12.12", "2.13.4")

  scalaVersions.foreach { scalaVersion =>
    println(s"--------- Scala Version: $scalaVersion")
    scalaVersionPaths(scalaVersion, vers => Paths.get(".", s"src-$vers")).foreach { path =>
      println(s"   => $path")
    }
  }
}
