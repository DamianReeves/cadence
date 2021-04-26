package cadence

final case class StepMetadata(name: Option[String], description: Option[String] = None) { self =>
  def changeName(name: String): StepMetadata =
    self.copy(name = Option(name).orElse(self.name))
}

object StepMetadata {
  val empty: StepMetadata = StepMetadata(name = None, description = None)
}
