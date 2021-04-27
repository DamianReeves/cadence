package cadence

import scala.annotation.implicitNotFound

/**
 * A value of type `CanProduceState[S]` provides implicit evidence that a step with
 * output state type `S` can produce/return a state value, that is, that `S` is not equal to `Nothing`.
 */
@implicitNotFound(
  "This operation assumes your step can produce state. However, " +
    "your effect has Nothing for the output state type, which means it cannot " +
    "produce state, so there is no way to interact with the output state."
)
sealed abstract class CanProduceState[-S]

object CanProduceState extends CanProduceState[Any] {

  implicit def canProduceState[S]: CanProduceState[S] = CanProduceState

  // Provide multiple ambiguous values so an implicit CanProduceState[Nothing] cannot be found.
  implicit val canProduceStateAmbiguous1: CanProduceState[Nothing] = CanProduceState
  implicit val canProduceStateAmbiguous2: CanProduceState[Nothing] = CanProduceState
}
