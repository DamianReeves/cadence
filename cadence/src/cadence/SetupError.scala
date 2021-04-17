package cadence
import zio.Cause

sealed trait SetupError[+A] extends Exception with Product with Serializable { self =>
  def cause: A

  def prettyPrint: String
}

object SetupError {
  final case class GetExecutionEnvironmentError[+A](commandLineArgs: List[String], cause: Cause[A])
      extends SetupError[Cause[A]] { self =>
    override def prettyPrint: String =
      s"""+=============================================================================
         ||[SetupError]: ${self.getClass().getCanonicalName()}
         ||--[Command Line Args]: $commandLineArgs
         ||--[Cause]: ${cause.prettyPrint}
         |+=============================================================================""".stripMargin
  }

  final case class ConfigurationError[+A](commandLineArgs: List[String], cause: Cause[A]) extends SetupError[Cause[A]] {
    self =>
    override def prettyPrint: String =
      s"""+=============================================================================
         ||[SetupError]: ${self.getClass().getCanonicalName()}
         ||--[Command Line Args]: $commandLineArgs
         ||--[Cause]: ${cause.prettyPrint}
         |+=============================================================================""".stripMargin
  }

  final case class LayerConstructionError[+A](cause: Cause[A]) extends SetupError[Cause[A]] { self =>
    override def prettyPrint: String =
      s"""+=============================================================================
         ||[SetupError]: ${self.getClass().getCanonicalName()}
         ||--[Cause]: ${cause.prettyPrint}
         |+=============================================================================""".stripMargin
  }
}
