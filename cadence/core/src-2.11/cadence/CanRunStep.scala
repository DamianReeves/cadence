package cadence

import scala.annotation.implicitNotFound

/**
 * A value of type `CanRunStep[E]` provides implicit evidence that an effect with
 * context type `Ctx` can fail, that is, that `E` is not equal to `Nothing`.
 */
@implicitNotFound(
  "This run operation assumes your step is runnable. However, " +
    "your effect has Nothing for the context type, which means it cannot run."
)
sealed abstract class CanRunStep[-Ctx]
object CanRunStep extends CanRunStep[Any] {
  implicit def canRunStep[Ctx]: CanRunStep[Ctx] = CanRunStep

  implicit val canRunStepAmbiguous1: CanRunStep[Nothing] = CanRunStep
  implicit val canRunStepAmbiguous2: CanRunStep[Nothing] = CanRunStep
}
