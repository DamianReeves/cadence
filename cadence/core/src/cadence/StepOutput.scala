package cadence

import cadence.StepOutput.StateOnly

sealed abstract class StepOutput[+S, +A] extends Product with Serializable { self =>

  def getState: Option[S] = self match {
    case StepOutput.Both(state, _)   => Some(state)
    case StepOutput.StateOnly(state) => Some(state)
    case StepOutput.ValueOnly(_)     => None
  }

  def getValue: Option[A] = self match {
    case StepOutput.Both(_, value)   => Some(value)
    case StepOutput.StateOnly(_)     => None
    case StepOutput.ValueOnly(value) => Some(value)
  }

  def mapState[S2](f: S => S2): StepOutput[S2, A] = self match {
    case output @ StepOutput.Both(state, _) => output.copy(state = f(state))
    case StepOutput.StateOnly(state)        => StateOnly(f(state))
    case output @ StepOutput.ValueOnly(_)   => output
  }

  def mapValue[B](f: A => B): StepOutput[S, B] = self match {
    case StepOutput.Both(state, value) => StepOutput.Both(state, f(value))
    case output @ StateOnly(_)         => output
    case StepOutput.ValueOnly(value)   => StepOutput.ValueOnly(f(value))
  }
}

object StepOutput {
  def both[S, A](state: S, value: A): StepOutput[S, A] = Both(state, value)

  def stateOnly[S](state: => S): StepOutput[S, Nothing] = StateOnly(state)

  def valueOnly[A](value: => A): StepOutput[Nothing, A] = ValueOnly(value)

  final case class Both[+S, +A](state: S, value: A) extends StepOutput[S, A]
  final case class StateOnly[+S](state: S)          extends StepOutput[S, Nothing]
  final case class ValueOnly[+A](value: A)          extends StepOutput[Nothing, A]
}
