package cadence
import zio._
trait StepExecutor {
  def nextSequenceNumber: UIO[StepSequenceNumber]
}

object StepExecutor {
  val nextSequenceNumber: ZIO[Has[StepExecutor], Nothing, StepSequenceNumber] =
    ZIO.serviceWith[StepExecutor](_.nextSequenceNumber)
}
