package cadence
import zio._
trait StepContext {
  def nextSequenceNumber: UIO[StepSequenceNumber]
}

object StepContext {
  val nextSequenceNumber: ZIO[Has[StepContext], Nothing, StepSequenceNumber] =
    ZIO.serviceWith[StepContext](_.nextSequenceNumber)
}
