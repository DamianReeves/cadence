package cadence

import zio.test._
import zio.test.Assertion._
object ZStepSpec extends DefaultRunnableSpec {
  def spec = suite("ZStep Spec")(
    suite("When constructing a ZStep")(
      testM("It should be possible to construct a success step")(
        assertM(ZStep.succeed("Yup!").behavior((), ()))(equalTo(((), "Yup!")))
      ),
      testM("It should be possible to construct a failed step")(
        assertM(ZStep.fail("Nope!").behavior((), ()).run)(fails(equalTo("Nope!")))
      ),
      testM("It should be possible to construct a get that retrieves the inital state") {
        val getStep = ZStep.get[Int]
        val value   = getStep.behavior(42, ())
        assertM(value)(equalTo(42 -> 42))
      }
    ),
    suite("When mapping a ZStep")(
      testM("Map should leave the state in tact") {
        val step = ZStep.succeed[List[String], Int](42).map(_ * 2.0)
        assertM(step.behavior(List.empty, 0))(equalTo(List.empty[String] -> 84.0))
      }
    ),
    suite("Running")(
      testM("Running should require a StepContext") {
        val step = ZStep.succeed[String, String]("Green")
        for {
          results <- step.run("", ())
        } yield assert(results)(equalTo("" -> "Green"))
      }
    )
  )
}
