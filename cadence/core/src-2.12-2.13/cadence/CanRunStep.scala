package cadence

import scala.annotation.implicitAmbiguous

sealed abstract class CanRunStep[-Ctx]
object CanRunStep extends CanRunStep[Any] {
  implicit def canRunStep[Ctx]: CanRunStep[Ctx] = CanRunStep

  // Provide multiple ambiguous values so an implicit CanFail[Nothing] cannot be found.
  @implicitAmbiguous(
    "This run operation assumes your step is runnable. However, " +
      "your effect has Nothing for the context type, which means it cannot run."
  )
  implicit val canRunStepAmbiguous1: CanRunStep[Nothing] = CanRunStep
  implicit val canRunStepAmbiguous2: CanRunStep[Nothing] = CanRunStep
}
