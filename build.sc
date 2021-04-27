import $ivy.`com.goyeau::mill-git:0.2.0`
import $ivy.`com.goyeau::mill-scalafix:0.2.0`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import com.goyeau.mill.git._
import com.goyeau.mill.scalafix.ScalafixModule
import io.github.davidgregory084._
import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import mill.scalalib.scalafmt._
import coursier.maven.MavenRepository
import ammonite.ops._, ImplicitWd._

object cadence extends Module {
  import Dependencies._
  import Versions._

  object jvm extends Cross[CadenceJvm](scala213, scala211, scala212) {}

  class CadenceJvm(val crossScalaVersion: String)
      extends CrossScalaModule
      with CommonJvmModule
      with CadencePublishModule { self =>

    def artifactName        = "cadence"
    def scalacPluginIvyDeps = Agg(com.github.ghik.`silencer-plugin`)
    def compileIvyDeps      = Agg(com.github.ghik.`silencer-lib`)
    def ivyDeps = Agg(
      dev.zio.zio
    )
    object test extends Tests {
      val crossScalaVersion: String = CadenceJvm.this.crossScalaVersion
    }
  }

  object core extends Module {
    object jvm extends Cross[CadenceCoreJvm](scala213, scala211, scala212) {}

    class CadenceCoreJvm(val crossScalaVersion: String)
        extends CrossScalaModule
        with CommonJvmModule
        with CadencePublishModule { self =>

      def artifactName        = "cadence-core"
      def scalacPluginIvyDeps = Agg(com.github.ghik.`silencer-plugin`)

      def compileIvyDeps = Agg(com.github.ghik.`silencer-lib`)
      def ivyDeps = Agg(
        org.`scala-lang`.modules.`scala-collection-compat`,
        dev.zio.zio
      )
      object test extends Tests {
        val crossScalaVersion: String = CadenceCoreJvm.this.crossScalaVersion
      }
    }
  }
}

object Dependencies {
  case object com {
    case object github {
      case object ghik {
        val silencerVersion   = "1.7.1"
        val `silencer-lib`    = ivy"com.github.ghik:::silencer-lib:$silencerVersion"
        val `silencer-plugin` = ivy"com.github.ghik:::silencer-plugin:$silencerVersion"
      }
    }
  }
  case object dev {
    case object zio {
      val version = "1.0.7"

      val zio            = ivy"dev.zio::zio:$version"
      val `zio-test`     = ivy"dev.zio::zio-test:$version"
      val `zio-test-sbt` = ivy"dev.zio::zio-test-sbt:$version"
    }
  }

  case object org {
    case object `scala-lang` {
      case object modules {
        val `scala-collection-compat` = ivy"org.scala-lang.modules::scala-collection-compat:2.4.3"
      }
    }
    case object scalamacros {
      val paradise = ivy"org.scalamacros:::paradise:2.1.1"
    }
  }
}

object Versions {
  val scala211  = "2.11.12"
  val scala212  = "2.12.13"
  val scala213  = "2.13.4"
  val scalaJS06 = "0.6.32"
  val scalaJS1  = "1.0.0"

  val scalaJVMVersions = Seq(scala211, scala212, scala213)

  val scalaJSVersions = Seq(
    (scala212, scalaJS06),
    (scala213, scalaJS06)
  )
}

object Sources {
  import scala.util.matching.Regex
  import mill.api.PathRef
  val ScalaVersionPattern: Regex = """([0-9]+)\.([0-9]+).*""".r

  def platformSpecificSources(givenPlatform: String, conf: String, baseDir: os.Path)(versions: String*) = {
    val platform = givenPlatform.toLowerCase()
    (for {
      version <- versions.toList
      srcPath <- conf match {
                   case "main" =>
                     List(
                       baseDir / "src",
                       baseDir / "src" / platform,
                       baseDir / s"src-$version",
                       baseDir / s"src-$version" / platform
                     )
                   case _ =>
                     List(
                       baseDir / conf / "src",
                       baseDir / conf / "src" / platform,
                       baseDir / conf / s"src-$version",
                       baseDir / conf / s"src-$version" / platform
                     )
                 }
      //if os.exists(srcPath)
    } yield srcPath).distinct.map(p => PathRef(p))
  }

  def crossPlatformSources(
    scalaVer: String,
    platform: String,
    conf: String,
    baseDir: os.Path,
    isDotty: Boolean = false
  ) = {
    val versions = partialVersion(scalaVer) match {
      case Some((2, 11)) =>
        List("2.11", "2.11+", "2.11-2.12", "2.x")
      case Some((2, 12)) =>
        List("2.12", "2.11+", "2.12+", "2.11-2.12", "2.12-2.13", "2.x")
      case Some((2, 13)) =>
        List("2.13", "2.11+", "2.12+", "2.13+", "2.12-2.13", "2.x")
      case _ if isDotty =>
        List("dotty", "2.11+", "2.12+", "2.13+", "3.x")
      case _ =>
        List()
    }
    platformSpecificSources(platform, conf, baseDir)(versions: _*)
  }

  def partialVersion(version: String) = version match {
    case ScalaVersionPattern(major, minor) => Option(major.toInt -> minor.toInt)
    case _                                 => None
  }
}

trait CadenceScalaModule extends ScalaModule with TpolecatModule { self =>
  import Dependencies._
  def scalacPluginIvyDeps = Agg(com.github.ghik.`silencer-plugin`)
  def compileIvyDeps      = Agg(com.github.ghik.`silencer-lib`)
  override def scalacOptions = T {
    super.scalacOptions().filterNot(Set("-Xlint:nullary-override"))
  }
}

trait CadenceScalafixModule extends ScalafixModule

trait CadencePublishModule extends GitVersionedPublishModule {
  def packageDescription = T(artifactName())
  def pomSettings = PomSettings(
    description = packageDescription(),
    organization = "org.morphir",
    url = "https://github.com/DamianReeves/cadence",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("DamianReeves", "cadence"),
    developers = Seq(
      Developer(
        "DamianReeves",
        "Damian Reeves",
        "https://github.com/DamianReeves"
      )
    )
  )
  def publishVersion: T[String] =
    GitVersionModule.version(withSnapshotSuffix = true)()
}

trait ScalaMacroModule extends ScalaModule {
  import Dependencies._
  def crossScalaVersion: String

  def scalacOptions = super.scalacOptions().toList ++ (
    if (crossScalaVersion.startsWith("2.13")) List("-Ymacro-annotations")
    else List.empty
  )

  abstract override def scalacPluginIvyDeps =
    super.scalacPluginIvyDeps() ++
      (if (crossScalaVersion.startsWith("2.12"))
         Agg(org.scalamacros.paradise)
       else
         Agg.empty)
}

trait CadenceCommonModule extends CadenceScalaModule with ScalafmtModule {

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )

  def platformSegment: String
}

trait CommonJvmModule extends CadenceCommonModule {
  def platformSegment = "jvm"
  def crossScalaVersion: String
  def isDotty: Boolean = false

  def millSourcePath = super.millSourcePath / os.up
  def sources = T.sources(
    super
      .sources()
      .++(Sources.crossPlatformSources(crossScalaVersion, platformSegment, "main", millSourcePath, isDotty))
      .distinct
  )
  trait Tests extends super.Tests with CadenceTestModule {
    def platformSegment = "jvm"
  }
}

trait CommonJsModule extends CadenceCommonModule with ScalaJSModule {
  def platformSegment = "js"
  def crossScalaJSVersion: String
  def scalaJSVersion = crossScalaJSVersion
  def millSourcePath = super.millSourcePath / os.up / os.up
  def sources = T.sources(
    super
      .sources()
      .flatMap(source =>
        Seq(
          PathRef(source.path),
          PathRef(source.path / os.up / platformSegment / source.path.last)
        )
      )
  )
  trait Tests extends super.Tests with CadenceTestModule {
    def platformSegment = "js"
    def scalaJSVersion  = crossScalaJSVersion
  }
}

trait CadenceTestModule extends CadenceScalaModule with TestModule {
  import Dependencies._
  def millSourcePath   = super.millSourcePath / os.up
  def isDotty: Boolean = false

  def crossScalaVersion: String
  def platformSegment: String

  def ivyDeps = Agg(
    dev.zio.`zio-test`,
    dev.zio.`zio-test-sbt`
  )

  def testFrameworks =
    Seq("zio.test.sbt.ZTestFramework")

  def offset: os.RelPath = os.rel
  def sources = T.sources(
    Sources.crossPlatformSources(crossScalaVersion, platformSegment, "test", millSourcePath, isDotty)
  )

  def resources = T.sources(
    super
      .resources()
      .flatMap(source =>
        Seq(
          PathRef(source.path / os.up / "test" / source.path.last)
        )
      )
      .distinct
  )
}
