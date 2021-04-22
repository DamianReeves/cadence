package cadence

final case class StepSequenceNumber(number: Long) extends AnyVal { self =>
  def increment: StepSequenceNumber = StepSequenceNumber(number + 1)
}

object StepSequenceNumber {
  def fromInt(number: Int): StepSequenceNumber   = StepSequenceNumber(number.toLong)
  def fromLong(number: Long): StepSequenceNumber = StepSequenceNumber(number)

  val zero: StepSequenceNumber = StepSequenceNumber(0)
}
