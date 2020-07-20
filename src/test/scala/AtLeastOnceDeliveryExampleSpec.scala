import akka.actor.ActorSystem

class AtLeastOnceDeliveryExampleSpec
  extends PersistentActorSpec(ActorSystem("AtLeastOnceDeliveryExample"))
{
  import AtLeastOnceDeliveryExample._

  "at-least-once-delivery-example" in {
    val destination = system.actorOf(MyDestination.props(20), "destination")
    val sender = system.actorOf(
      MyPersistentActor.props(
        system.actorSelection(destination.path)
      )
    )

    sender ! "msg1"
    sender ! "msg2"
    sender ! "msg3"

    Thread.sleep(10000)
  }
}
