import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ExamplePersistentActorSpec
  extends PersistentActorSpec(ActorSystem("ExamplePersistentActor"))
{
  "should persist and recover" in {
    val numOfCommands = 15

    val actor = system.actorOf(ExamplePersistentActor.props())
    (1 to numOfCommands).foreach { i =>
      actor ! ExamplePersistentActor.Command(s"test$i")
    }
    actor ! "get"
    val state = expectMsgType[ExamplePersistentActor.State]
    state.events.size shouldBe numOfCommands

    kill(actor)

    val recoveredActor = system.actorOf(ExamplePersistentActor.props())
    recoveredActor ! "get"
    val recoveredState = expectMsgType[ExamplePersistentActor.State]
    recoveredState.events.size shouldBe numOfCommands
  }
}
