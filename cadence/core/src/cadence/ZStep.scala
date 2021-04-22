package cadence
import zio._

abstract class ZStep[-SIn, +SOut, -P, -R, +E, +A] { self =>
  import ZStep._

  /**
   * Get this `ZStep` as an effect.
   */
  protected[cadence] lazy val asEffect: ZIO[(SIn, P, R), E, (SOut, A)] = toEffect

  /**
   * Defines the underlying behavior of this `ZStep`.
   */
  protected[cadence] def behavior(state: SIn, parameters: P): ZIO[R, E, (SOut, A)]

  /**
   * Returns a `ZStep` whose success is mapped by the specified function f.
   */
  final def map[B](f: A => B): ZStep[SIn, SOut, P, R, E, B] = fromBehavior(self.asEffect.map { case (state, value) =>
    (state, f(value))
  })

  /**
   * Transform this `ZStep` into an effect by applying the behavior.
   */
  def toEffect: ZIO[(SIn, P, R), E, (SOut, A)] = ZIO.accessM[(SIn, P, R)] { case (stateIn, params, r) =>
    behavior(stateIn, params).provide(r)
  }

}

object ZStep {
  def fromBehavior[SIn, SOut, P, R, E, A](effect: ZIO[(SIn, P, R), E, (SOut, A)]): ZStep[SIn, SOut, P, R, E, A] =
    FromBehavior(effect)

  def fail[E](error: => E): IOStep[Nothing, E, Nothing]          = new Fail(error)
  def get[S]: Step[S, S, Any, Any, Nothing, S]                   = new Get()
  def setOutputs[S, A](state: S, value: A): UStep[S, A]          = new SetOutputs(stateOut = state, value = value)
  def succeed[S, A](value: A): ZStep[S, S, Any, Any, Nothing, A] = new Succeed[S, A](value)

  final case class FromBehavior[-SIn, +SOut, -P, -R, +E, +A](
    private val effect: ZIO[(SIn, P, R), E, (SOut, A)]
  ) extends ZStep[SIn, SOut, P, R, E, A] {
    protected[cadence] def behavior(state: SIn, parameters: P): ZIO[R, E, (SOut, A)] =
      effect.provideSome[R](r => (state, parameters, r))
  }

  private final class Fail[+E](error: E) extends IOStep[Nothing, E, Nothing] {
    protected[cadence] def behavior(state: Any, parameters: Any): ZIO[Any, E, (Nothing, Nothing)] = ZIO.fail(error)
  }

  private final class Get[S] extends Step[S, S, Any, Any, Nothing, S] {
    protected[cadence] def behavior(state: S, parameters: Any): ZIO[Any, Nothing, (S, S)] = ZIO.succeed(state -> state)
  }

  private final class SetOutputs[S, A](stateOut: S, value: A) extends UStep[S, A] {
    protected[cadence] def behavior(state: Any, parameters: Any): ZIO[Any, Nothing, (S, A)] =
      ZIO.succeed(stateOut -> value)

  }

  private final class Succeed[S, A](value: A) extends ZStep[S, S, Any, Any, Nothing, A] {
    protected[cadence] def behavior(state: S, parameters: Any): ZIO[Any, Nothing, (S, A)] =
      ZIO.succeed(state -> value)

  }

  implicit class RunnableStep[SIn, SOut, P, R, E, A](val self: ZStep[SIn, SOut, P, Has[StepContext] with R, E, A])
      extends AnyVal {
    def run(state: SIn, parameters: P): ZIO[Has[StepContext] with R, E, (SOut, A)] =
      for {
        _       <- StepContext.nextSequenceNumber
        results <- self.behavior(state, parameters)
      } yield results
  }
}
