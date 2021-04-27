package cadence

import zio.test._
import zio.test.Assertion._
object StepOutputSpec extends DefaultRunnableSpec {
  def spec = suite("StepOutput Spec")(
    testM("Calling state_ should work if only state is available") {
      val sut = StepOutput.stateOnly(BlankState())
      for {
        state <- sut.state_
      } yield assert(state)(Assertion.equalTo(BlankState()))
    },
    testM("Calling state_ should work if both state and a value is available") {
      val sut = StepOutput.both(42, "Test")
      for {
        state <- sut.state_
      } yield assert(state)(Assertion.equalTo(42))
    },
    testM("Calling state_ should not work if the output is a value only") {
      val result = typeCheck {
        """
           import cadence._
           val sut = StepOutput.valueOnly(42)
           sut.state_
        """
      }
      assertM(result)(isLeft(anything))
    },
    testM("Calling value_ should work if only value is available") {
      val inputValue = ('a', 'b', 'c')
      val sut        = StepOutput.valueOnly(inputValue)
      for {
        value <- sut.value_
      } yield assert(value)(Assertion.equalTo(inputValue))
    },
    testM("Calling value_ should work if both state and a value is available") {
      val sut = StepOutput.both(42, "Test")
      for {
        value <- sut.value_
      } yield assert(value)(Assertion.equalTo("Test"))
    },
    testM("Calling value_ should not compile if only state is available") {
      val result = typeCheck {
        """
           import cadence._
           val sut = StepOutput.stateOnly(42)
           sut.value_
        """
      }
      assertM(result)(isLeft(anything))
    }
  )

  final case class BlankState()
}
