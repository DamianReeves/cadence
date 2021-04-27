package cadence
import scala.annotation.implicitAmbiguous

/**
 * A value of type `CanProduceState[S]` provides implicit evidence that a step with
 * output state type `S` can fail, that is, that `S` is not equal to `Nothing`.
 */
sealed abstract class CanProduceValue[-S]

object CanProduceValue extends CanProduceValue[Any] {

  implicit def canProduceValue[S]: CanProduceValue[S] = CanProduceValue

  // Provide multiple ambiguous values so an implicit CanProduceValue[Nothing] cannot be found.
  @implicitAmbiguous(
    "This operation assumes your step can produce an output value. However, " +
      "your step has Nothing for the output value type, which means it cannot " +
      "produce an output value, so there is no way to interact with the value."
  )
  implicit val canProduceValueAmbiguous1: CanProduceValue[Nothing] = CanProduceValue
  implicit val canProduceValueAmbiguous2: CanProduceValue[Nothing] = CanProduceValue
}
