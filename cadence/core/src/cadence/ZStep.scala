package cadence
import zio._

final case class ZStep[-SIn, +SOut, -P, -R, +E, +A] private (
  private val behavior: ZIO[(StepExecutionContext, SIn, P, R), E, (SOut, A)],
  private val metadata: StepMetadata
) { self =>
  //import ZStep._

  def >>>[SOut2, R1 <: R, E1 >: E, B](that: ZStep[SOut, SOut2, A, R1, E1, B]): ZStep[SIn, SOut2, P, R1, E1, B] =
    self andThen that

  def *>[SIn1 <: SIn, P1 <: P, R1 <: R, E1 >: E, SOut1, B](
    that: ZStep[SIn1, SOut1, P1, R1, E1, B]
  ): ZStep[SIn1, SOut1, P1, R1, E1, B] =
    ZStep(self.behavior *> that.behavior)

  def andThen[SOut2, R1 <: R, E1 >: E, B](that: ZStep[SOut, SOut2, A, R1, E1, B]): ZStep[SIn, SOut2, P, R1, E1, B] =
    ZStep(behavior = ZIO.accessM[(StepExecutionContext, SIn, P, R1)] { case (context, _, _, r) =>
      self.behavior.flatMap { case (sOut, a) =>
        that.behavior.provide((context, sOut, a, r))
      }
    })

  def as[B](value: B): ZStep[SIn, SOut, P, R, E, B] = self.map(_ => value)

  def flatMap[SOut1, P1 <: P, R1 <: R, E1 >: E, B](
    continuation: A => ZStep[SOut, SOut1, P1, R1, E1, B]
  ): ZStep[SIn, SOut1, P1, R1, E1, B] =
    ZStep[SIn, SOut1, P1, R1, E1, B](
      behavior = ZIO.accessM[(StepExecutionContext, SIn, P1, R1)] { case (context, _, p, r) =>
        behavior.flatMap { case (stateOutA, a) => continuation(a).behavior.provide((context, stateOutA, p, r)) }
      }
    )

  /**
   * Returns a `ZStep` whose success is mapped by the specified function f.
   */
  def map[B](f: A => B): ZStep[SIn, SOut, P, R, E, B] = self.copy(behavior = self.behavior.map { case (state, value) =>
    (state, f(value))
  })

  def changeName(name: String): ZStep[SIn, SOut, P, R, E, A] =
    ZStep(behavior = behavior, metadata = self.metadata.changeName(name))

  def provideState(state: SIn): ZStep[Any, SOut, P, R, E, A] =
    ZStep(
      behavior = ZIO.accessM[(StepExecutionContext, Any, P, R)] { case (context, _, p, r) =>
        self.behavior.provide((context, state, p, r))
      },
      metadata = self.metadata
    )

  def provideStateLayer[E1 >: E](layer: Layer[E1, SIn]): ZStep[Any, SOut, P, R, E1, A] = ZStep(
    behavior = ZIO.accessM[(StepExecutionContext, Any, P, R)] { case (context, _, p, r) =>
      self.behavior.provideLayer(layer.map(state => (context, state, p, r)))
    },
    metadata = self.metadata
  )

  def run(state: SIn, parameters: P): ZIO[R, E, (SOut, A)] =
    for {
      r            <- ZIO.environment[R]
      seqNumber    <- StepSequenceNumberGenerator.next
      log          <- Ref.make(Chunk[String]())
      runningSteps <- Ref.make(Set.empty[StepMetadata])
      context       = StepExecutionContext(seqNumber, log, runningSteps)
      results      <- behavior.provide((context, state, parameters, r))
    } yield results

  //private def runStepInstance(identifier: StepIdentifier) =
}

object ZStep {

  def apply[SIn, SOut, P, R, E, A](
    behavior: ZIO[(StepExecutionContext, SIn, P, R), E, (SOut, A)]
  ): ZStep[SIn, SOut, P, R, E, A] =
    ZStep(behavior, metadata = StepMetadata.empty)

  def executionContext[S]: ZStep[S, S, Any, Any, Nothing, StepExecutionContext] = ZStep(
    behavior = ZIO.access[(StepExecutionContext, S, Any, Any)](env => (env._2, env._1)),
    metadata = StepMetadata.empty
  )

  def fail[E](error: => E): IOStep[Nothing, E, Nothing] = ZStep(behavior = ZIO.fail(error))

  def fromBehavior[SIn, SOut, P, R, E, A](behavior: Behavior[SIn, SOut, P, R, E, A]): ZStep[SIn, SOut, P, R, E, A] =
    ZStep(behavior = behavior, metadata = StepMetadata.empty)

  def fromFunction[In, Out](f: In => Out): ZStep[Any, Any, In, Any, Throwable, Out] = ZStep(
    ZIO.accessM[(StepExecutionContext, Any, In, Any)](env => ZIO.effect((env._2, f(env._3))))
  )
//
//  def fromEffect[S, R, E, A](effect: ZIO[R, E, (S, A)]) = ???
//
  def get[S]: ZStep[S, S, Any, Any, Nothing, S] =
    ZStep(behavior = ZIO.access[(Any, S, Any, Any)](env => (env._2, env._2)))

  def getState[S: Tag]: ZStep[State[S], State[S], Any, Any, Nothing, S] = ZStep(
    behavior = ZIO.accessM[(StepExecutionContext, State[S], Any, Any)](env => env._2.get.get.map(v => (env._2, v)))
  )

  def setOutputs[S, A](state: S, value: A): UStep[S, A] = ZStep(ZIO.succeed((state, value)))

  def succeed[S]: SucceedPartiallyApplied[S] = new SucceedPartiallyApplied[S]

  def stateWith[S]: StateWithPartiallyApplied[S] = new StateWithPartiallyApplied[S]

  final class StateWithPartiallyApplied[SIn](private val dummy: Boolean = true) extends AnyVal {
    def apply[SOut, P, R, E, A](
      f: SIn => ZStep[State[SIn], SOut, P, R, E, A]
    )(implicit ev: Tag[StateRef[SIn]]): ZStep[State[SIn], SOut, P, R, E, A] =
      ZStep(ZIO.accessM[(StepExecutionContext, State[SIn], P, R)] { case (_, stateIn, _, _) =>
        for {
          sIn    <- stateIn.get[StateRef[SIn]].get
          result <- f(sIn).behavior
        } yield result
      })
  }

  final class SucceedPartiallyApplied[S](private val dummy: Boolean = true) extends AnyVal {
    def apply[A](value: => A): ZStep[S, S, Any, Any, Nothing, A] = ZStep(
      ZIO.access[(StepExecutionContext, S, Any, Any)](env => (env._2, value))
    )
  }

//  implicit class RunnableStep[SIn, SOut, P, R, E, A](val self: ZStep[SIn, SOut, P, Has[StepExecutor] with R, E, A])
//      extends AnyVal {
//    def run(state: SIn, parameters: P): ZIO[Has[StepExecutor] with R, E, (SOut, A)] =
//      for {
//        _       <- StepExecutor.nextSequenceNumber
//        results <- self.behavior(state, parameters)
//      } yield results
//  }
}
