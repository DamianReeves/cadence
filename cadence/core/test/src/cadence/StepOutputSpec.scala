package cadence

import zio.test._
import zio.test.Assertion._
object StepOutputSpec extends DefaultRunnableSpec {
  def spec = suite("StepOutput Spec")(
    testM("Calling stateToEffect should work if both state and a value is available") {
      val sut = StepOutput.both(42, "Test")
      for {
        state <- sut.stateToEffect
      } yield assert(state)(Assertion.equalTo(42))
    },
    testM("Calling stateToEffect should not work if the output is a value only") {
      val result = typeCheck {
        """
           import cadence._
           val sut = StepOutput.valueOnly(42)
           sut.stateToEffect
        """
      }
      assertM(result)(isLeft(anything))
    },
    testM("Calling valueToEffect should work if only value is available") {
      val inputValue = ('a', 'b', 'c')
      val sut        = StepOutput.valueOnly(inputValue)
      for {
        value <- sut.valueToEffect
      } yield assert(value)(Assertion.equalTo(inputValue))
    },
    testM("Calling valueToEffect should work if both state and a value is available") {
      val sut = StepOutput.both(42, "Test")
      for {
        value <- sut.valueToEffect
      } yield assert(value)(Assertion.equalTo("Test"))
    },
    testM("Calling valueToEffect should not compile if only state is available") {
      val result = typeCheck {
        """
           import cadence._
           val sut = StepOutput.stateOnly(42)
           sut.valueToEffect
        """
      }
      assertM(result)(isLeft(anything))
    }
  )

  final case class BlankState()
}
