package cadence

import scala.annotation.implicitAmbiguous

/**
 * A value of type `CanProduceState[S]` provides implicit evidence that a step with
 * output state type `S` can fail, that is, that `S` is not equal to `Nothing`.
 */
sealed abstract class CanProduceState[-S]

object CanProduceState extends CanProduceState[Any] {

  implicit def canProduceState[S]: CanProduceState[S] = CanProduceState

  // Provide multiple ambiguous values so an implicit CanProduceValue[Nothing] cannot be found.
  @implicitAmbiguous(
    "This operation assumes your step can produce state. However, " +
      "your effect has Nothing for the output state type, which means it cannot " +
      "produce state, so there is no way to interact with the output state."
  )
  implicit val canProduceStateAmbiguous1: CanProduceState[Nothing] = CanProduceState
  implicit val canProduceStateAmbiguous2: CanProduceState[Nothing] = CanProduceState
}
