package cadence
import zio._

abstract class ZStep[-SIn, +SOut, -P, -R, +E, +A] { self =>
  import ZStep._

  protected[cadence] def behavior(state: SIn, parameters: P): ZIO[R, E, (SOut, A)]
}

object ZStep {
  def fail[E](error: => E): IOStep[Nothing, E, Nothing] = new Fail(error)
  def succeed[S, A](state: S, value: A): UStep[S, A]    = new Succeed(state, value)

  final class Fail[+E](error: E) extends IOStep[Nothing, E, Nothing] {
    protected[cadence] def behavior(state: Any, parameters: Any): ZIO[Any, E, (Nothing, Nothing)] = ZIO.fail(error)
  }

  private final class Succeed[S, A](stateOut: S, value: A) extends UStep[S, A] {

    protected[cadence] def behavior(state: Any, parameters: Any): ZIO[Any, Nothing, (S, A)] =
      ZIO.succeed(stateOut -> value)

  }
}
