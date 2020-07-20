import akka.actor.ActorRef
import akka.testkit.TestKitBase

trait ActorKillerSupport { this : TestKitBase =>
  def kill(actors: ActorRef*): Unit = {
    actors.foreach { actor =>
      watch(actor)
      system.stop(actor)
      expectTerminated(actor)
    }
  }
}
