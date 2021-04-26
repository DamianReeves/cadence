package cadence

import zio._

final case class StateRef[A](fiberRef: FiberRef[A]) extends AnyVal {
  def get: UIO[A]                  = fiberRef.get
  def update(f: A => A): UIO[Unit] = fiberRef.update(f)
  def set(value: A): UIO[Unit]     = fiberRef.set(value)
}

object StateRef {
  def make[A](value: A): UIO[StateRef[A]] =
    FiberRef.make(value).map(StateRef(_))
}
