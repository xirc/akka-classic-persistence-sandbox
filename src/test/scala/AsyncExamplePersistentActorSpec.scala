import akka.actor.ActorSystem

class AsyncExamplePersistentActorSpec
  extends PersistentActorSpec(ActorSystem("AsyncExamplePersistentActor"))
{
  "many commands" in {
    // See logging
    val actor = system.actorOf(AsyncExamplePersistentActor.props())

    val numOfCommands = 10
    val commands = (1 to numOfCommands).map(_.toString)
    commands foreach { command =>
      actor ! command
    }

    (1 to numOfCommands*4) foreach { _ =>
      val msg = expectMsgType[String]
      println(msg)
    }
  }
}
