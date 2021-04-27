package cadence

import zio._

import scala.annotation.nowarn
sealed abstract class StepOutput[+S, +A](value: A) extends Product with Serializable { self =>

  def getState: Option[S] = self match {
    case StepOutput.Both(state, _) => Some(state)
    case StepOutput.ValueOnly(_)   => None
  }

  final def flatMap[S1, B](f: A => StepOutput[S1, B]): StepOutput[S1, B] =
    f(value)

  final def map[B](f: A => B): StepOutput[S, B] = self match {
    case StepOutput.Both(state, value) => StepOutput.Both(state, f(value))
    case StepOutput.ValueOnly(value)   => StepOutput.ValueOnly(f(value))
  }

  def mapState[S2](f: S => S2): StepOutput[S2, A] = self match {
    case output @ StepOutput.Both(state, _) => output.copy(state = f(state))
    case output @ StepOutput.ValueOnly(_)   => output
  }

  def setValue[B](newValue: B): StepOutput[S, B] = self match {
    case StepOutput.Both(state, _) => StepOutput.Both(state, newValue)
    case _                         => StepOutput.valueOnly(newValue)
  }
  def stateToEffect(implicit @nowarn ev: CanProduceState[S]): UIO[S] = ZIO.effectTotal(getState.get)
  def valueToEffect: UIO[A]                                          = ZIO.succeed(value)
}

object StepOutput {
  def both[S, A](state: S, value: A): StepOutput[S, A]  = Both(state, value)
  def valueOnly[A](value: => A): StepOutput[Nothing, A] = ValueOnly(value)

  final case class Both[+S, +A](state: S, value: A) extends StepOutput[S, A](value)
  final case class ValueOnly[+A](value: A)          extends StepOutput[Nothing, A](value)
}
