package cadence.core

import cadence.{ StepSequenceNumber, StepSequenceNumberGenerator }
import zio.{ Ref, UIO }

final case class StepSequenceNumberGeneratorLive(number: Ref[StepSequenceNumber]) extends StepSequenceNumberGenerator {
  def next: UIO[StepSequenceNumber] = number.modify { num =>
    val next = num.increment
    (next, next)
  }
}

object StepSequenceNumberGeneratorLive {
  def make(initial: StepSequenceNumber = StepSequenceNumber.zero): UIO[StepSequenceNumberGenerator] =
    Ref.make(initial).map(StepSequenceNumberGeneratorLive(_))
}
