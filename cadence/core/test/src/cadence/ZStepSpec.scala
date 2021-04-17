package cadence

import zio.test._
import zio.test.Assertion._
object ZStepSpec extends DefaultRunnableSpec {
  def spec = suite("ZStep Spec")(
    suite("When constructing a ZStep")(
      testM("It should be possible to construct a failed step")(
        assertM(ZStep.fail("Nope!").behavior((), ()).run)(fails(equalTo("Nope!")))
      )
    )
  )
}
