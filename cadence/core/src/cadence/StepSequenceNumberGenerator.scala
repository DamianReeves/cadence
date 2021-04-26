package cadence

import zio._

import java.util.concurrent.atomic.AtomicLong

object StepSequenceNumberGenerator {
  private val counter: AtomicLong = new AtomicLong()

  val next: UIO[StepSequenceNumber] =
    UIO.effectTotal(StepSequenceNumber.fromLong(counter.incrementAndGet()))

}
