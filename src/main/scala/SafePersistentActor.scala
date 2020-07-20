import SafePersistentActor.Shutdown
import akka.actor.Props
import akka.persistence.PersistentActor

object SafePersistentActor {
  def props(): Props = Props(new SafePersistentActor)

  case object Shutdown
}
class SafePersistentActor extends PersistentActor {
  override def persistenceId: String = "safe-actor"

  override def receiveRecover: Receive = {
    case _ => // handle recovery here
  }
  override def receiveCommand: Receive = {
    case command: String  =>
      println(command)
      persist(s"handle-$command") { e =>
        println(e)
      }
    case Shutdown =>
      context.stop(self)
  }
}
