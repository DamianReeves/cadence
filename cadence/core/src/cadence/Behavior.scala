package cadence
import zio._
object Behavior {

  val context: UBehavior[Any, StepExecutionContext] =
    ZIO.access[(StepExecutionContext, Any, Any, Any)](env => (env._2, env._1))

  def fail[E](error: => E): IOBehavior[Any, E, Nothing] = ZIO.fail(error)
  def environment[R]                                    = ZIO.access[(Any, Any, Any, R)](_._4)
  def get[S]                                            = ZIO.access[(Any, S, Any, Any)](_._2)
  def parameters[P]                                     = ZIO.access[(Any, Any, P, Any)](_._3)

  def succeed[S, A](state: S, value: A): Behavior[Any, S, Any, Any, Nothing, A] = ZIO.succeed((state, value))
}
