import zio._
package object cadence {
  type Behavior[-SIn, +SOut, -P, -R, +E, +A] = ZIO[(StepExecutionContext, SIn, P, R), E, (SOut, A)]
  type IOBehavior[+SOut, +E, +A]             = ZIO[(StepExecutionContext, Any, Any, Any), E, (SOut, A)]
  type UBehavior[+SOut, +A]                  = URIO[(StepExecutionContext, Any, Any, Any), (SOut, A)]
  //type Step[-SIn, +SOut, -P, -R, +E, +A]     = ZStep[SIn, SOut, P, R, E, A]
  type IOStep[+S, +E, +A] = ZStep[Any, S, Any, Any, E, A]
  type UStep[+S, +A]      = ZStep[Any, S, Any, Any, Nothing, A]

  type State[A] = Has[StateRef[A]]
}
