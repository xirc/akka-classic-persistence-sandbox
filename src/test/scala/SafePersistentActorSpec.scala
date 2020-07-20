import akka.actor.{ActorSystem, PoisonPill}

class SafePersistentActorSpec
  extends PersistentActorSpec(ActorSystem("safe-persistent-actor"))
{
  "unsafe way" in {
    val actor = system.actorOf(SafePersistentActor.props())
    watch(actor)

    actor ! "a"
    actor ! "b"
    actor ! PoisonPill

    expectTerminated(actor)
  }

  "safe way" in {
    val actor = system.actorOf(SafePersistentActor.props())
    watch(actor)

    actor ! "a"
    actor ! "b"
    actor ! SafePersistentActor.Shutdown

    expectTerminated(actor)
  }

}
