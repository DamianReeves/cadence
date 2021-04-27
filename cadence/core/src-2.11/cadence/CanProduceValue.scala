package cadence

import scala.annotation.implicitNotFound

/**
 * A value of type `CanProduceValue[A]` provides implicit evidence that a step with
 * return type `A` can succeed with a value, that is, that `A` is not equal to `Nothing`.
 */
@implicitNotFound(
  "This operation assumes your step can produce an output value. However, " +
    "your step has Nothing for the output value type, which means it cannot " +
    "produce an output value, so there is no way to interact with the value."
)
sealed abstract class CanProduceValue[-A]

object CanProduceValue extends CanProduceValue[Any] {
  implicit def canProduceValue[A]: CanProduceValue[A] = CanProduceValue

  // Provide multiple ambiguous values so an implicit CanProduceValue[Nothing] cannot be found.
  implicit val canProduceValueAmbiguous1: CanProduceValue[Nothing] = CanProduceValue
  implicit val canProduceValueAmbiguous2: CanProduceValue[Nothing] = CanProduceValue
}
