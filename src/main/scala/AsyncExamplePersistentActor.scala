import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import scala.collection.immutable.Seq

object AsyncExamplePersistentActor {
  def props(): Props = Props(new AsyncExamplePersistentActor)
}
class AsyncExamplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String =  "async-persistent-actor-id"

  override def receiveRecover: Receive = {
    case _ => // handle recovery here
  }
  override def receiveCommand: Receive = receiveB

  private def receiveA: Receive = {
    case command: String =>
      // Order is guaranteed
      // event-*-1
      // event-*-2
      // event-*-3
      sender() ! command
      persistAsync(s"event-$command-1") { e =>
        sender() ! e
      }
      persistAsync(s"event-$command-2") { e =>
        sender() ! e
      }
      deferAsync(s"event-$command-3") { e =>
        sender() ! e
      }
  }

  private def receiveB: Receive = {
    case command: String =>
      sender() ! command
      // Atomic writes. Some journals may not supported.
      persistAllAsync(Seq(s"event-$command-1", s"event-$command-2")) { e =>
        sender() ! e
      }
      deferAsync(s"event-$command-3") { e =>
        sender() ! e
      }
  }
}
