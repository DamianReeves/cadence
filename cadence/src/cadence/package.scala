package object cadence {
  type IOStep[+S, +E, +A] = ZStep[Any, S, Any, Any, E, A]
  type UStep[+S, +A]      = ZStep[Any, S, Any, Any, Nothing, A]
}
