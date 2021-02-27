//#full-example
package akka1.iot

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka1.hello.Greeter
import akka1.hello.Greeter.{Greet, Greeted}
import org.scalatest.wordspec.AnyWordSpecLike

//#definition
class AkkaQuickstartSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike {
//#definition

  "A Greeter" must {
    //#test
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
    //#test
  }

}
//#full-example
