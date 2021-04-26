package cadence

import zio._

final case class StateRef[A](fiberRef: FiberRef[A]) extends AnyVal {
  def get: UIO[A]              = fiberRef.get
  def set(value: A): UIO[Unit] = fiberRef.set(value)
}
