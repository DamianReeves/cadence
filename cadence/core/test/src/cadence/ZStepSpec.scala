package cadence

import zio.test._
import zio.test.Assertion._
object ZStepSpec extends DefaultRunnableSpec {
  def spec = suite("ZStep Spec")(
    suite("When constructing a ZStep")(
      testM("It should be possible to construct a success step")(
        assertM(ZStep.succeed("Yup!").run((), ()))(equalTo(StepOutput.both((), "Yup!")))
      ),
      testM("It should be possible to construct a failed step")(
        assertM(ZStep.fail("Nope!").run((), ()).run)(fails(equalTo("Nope!")))
      ),
      testM("It should be possible to construct a get that retrieves the initial state") {
        val getStep = ZStep.get[Int]
        val value   = getStep.run(42, ())
        assertM(value)(equalTo(StepOutput.both(42, 42)))
      },
      testM("It should be possible to use stateWith to access the state for use within a step") {
        val stepUnderTest = ZStep.stateWith[List[Int]](givenState => ZStep.succeed(givenState.map(_ * 10)))
        for {
          initialState <- State.make(List(1, 2, 3))
          result       <- stepUnderTest.run(initialState, ())
        } yield assert(result)(equalTo(StepOutput.both(initialState, List(10, 20, 30))))
      }
    ),
    suite("When mapping a ZStep")(
      testM("Map should leave the state intact") {
        val step = ZStep.succeed[List[String]](42).map(_ * 2.0)
        assertM(step.run(List.empty, 0))(equalTo(StepOutput.both(List.empty[String], 84.0)))
      }
    )
  )
}
