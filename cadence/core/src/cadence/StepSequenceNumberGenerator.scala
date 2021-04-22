package cadence

import cadence.core.StepSequenceNumberGeneratorLive
import zio._

trait StepSequenceNumberGenerator {
  def next: UIO[StepSequenceNumber]
}

object StepSequenceNumberGenerator {

  val next: ZIO[Has[StepSequenceNumberGenerator], Nothing, StepSequenceNumber] =
    ZIO.serviceWith[StepSequenceNumberGenerator](_.next)

  val live: ULayer[Has[StepSequenceNumberGenerator]] =
    StepSequenceNumberGeneratorLive.make().toLayer
}
