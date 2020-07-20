import akka.actor.{ActorLogging, Props}
import akka.persistence.{DeleteMessagesFailure, DeleteMessagesSuccess, PersistentActor, Recovery, RecoveryCompleted, SnapshotOffer, SnapshotSelectionCriteria}

object ExamplePersistentActor {
  def props(): Props = Props(new ExamplePersistentActor)

  case class Command(data: String)
  case class Event(data: String)

  case class State(events: List[String] = Nil) {
    def updated(event: Event): State = copy(event.data :: events)
    def size: Int = events.size
    override def toString: String = events.reverse.toString
  }
}
final class ExamplePersistentActor extends PersistentActor with ActorLogging {
  import ExamplePersistentActor._

  override def persistenceId: String = "sample-id-1"

  private var state = State()

  private def numOfEvents = state.size
  private val snapshotInterval = 10

  // Disable Snapshot Recovery for incompatible snapshot upgrades
  // override def recovery: Recovery =
  //   Recovery(SnapshotSelectionCriteria.None)

  private def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveRecover: Receive = {
    case event: Event =>
      log.info("Recovering using {} ...", event)
      logRecoveryStatus()
      updateState(event)
    case SnapshotOffer(metadata, snapshot: State) =>
      log.info("Recovering from Snapshot {} {}", metadata, snapshot)
      logRecoveryStatus()
      state = snapshot
    case RecoveryCompleted =>
      logRecoveryStatus()
      log.info("Recovery Completed !")
    case DeleteMessagesFailure =>
      log.info("Delete Message Failure")
    case DeleteMessagesSuccess =>
      log.info("Delete Message Success")
  }

  override def receiveCommand: Receive = {
    case Command(data) =>
      persist(Event(s"$data-$numOfEvents")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        val shouldSaveSnapshot =
          (lastSequenceNr % snapshotInterval == 0) && (lastSequenceNr != 0)
        if (shouldSaveSnapshot) {
          saveSnapshot(state)
        }
      }
    case "get" =>
      log.info("{}", state)
      sender() ! state
    case "delete" =>
      // this is not good way in most case.
      deleteMessages(snapshotSequenceNr)
  }

  private def logRecoveryStatus(): Unit = {
    log.info("Recovery Running {}", recoveryRunning)
    log.info("Recovery Finished {}", recoveryFinished)
  }

}
