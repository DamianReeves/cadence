package object cadence {
  type Step[-SIn, +SOut, -P, -R, +E, +A] = ZStep[Nothing, SIn, SOut, P, R, E, A]
  type IOStep[+S, +E, +A]                = ZStep[Nothing, Any, S, Any, Any, E, A]
  type UStep[+S, +A]                     = ZStep[Nothing, Any, S, Any, Any, Nothing, A]
}
