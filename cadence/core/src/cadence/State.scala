package cadence

import zio._

object State {
  def make[A](value: A)(implicit ev: Tag[StateRef[A]]): UIO[State[A]] =
    StateRef.make(value).map(Has(_))
}
