package cadence
import zio._

final case class StepExecutionContext(
  sequenceNumber: StepSequenceNumber,
  log: Ref[Chunk[String]],
  current: Ref[Set[StepMetadata]]
)

object StepExecutionContext {}
