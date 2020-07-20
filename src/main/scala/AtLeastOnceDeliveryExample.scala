import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import akka.persistence.AtLeastOnceDelivery.UnconfirmedWarning
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}

import scala.concurrent.duration._
import scala.util.Random

object AtLeastOnceDeliveryExample {
  case class Message(deliveryId: Long, value: String)
  case class Confirm(deliveryId: Long)

  sealed trait Event
  case class MessageSent(value: String) extends Event
  case class MessageConfirmed(deliveryId: Long) extends Event

  object MyPersistentActor {
    def props(destination: ActorSelection): Props =
      Props(new MyPersistentActor(destination))
  }
  class MyPersistentActor(destination: ActorSelection)
    extends PersistentActor with AtLeastOnceDelivery with ActorLogging
  {
    override def persistenceId: String = "persistence-id"
    private var sentMessage: Int = 0

    // akka.persistence.at-least-once-delivery.redeliver-interval
    override def redeliverInterval = 1 seconds
    // akka.persistence.at-least-once-delivery.redelivery-burst-limit
    override def redeliveryBurstLimit: Int = 2
    // akka.persistence.at-least-once-delivery.warn-after-number-of-unconfirmed-attempts
    override def warnAfterNumberOfUnconfirmedAttempts: Int = 3

    override def receiveCommand: Receive = {
      case value: String =>
        persist(MessageSent(value))(updateState)
      case msg @ Confirm(deliveryId) =>
        persist(MessageConfirmed(deliveryId))(updateState)
      case UnconfirmedWarning(deliveries) =>
        log.warning("Many Unconfirmed messages {}", deliveries)
    }

    override def receiveRecover: Receive = {
      case event: Event =>
        updateState(event)
    }

    private def updateState(event: Event): Unit = event match {
      case MessageSent(value) =>
        log.info("Deliver({})", value)
        deliver(destination) {
          deliveryId => Message(deliveryId, value)
        }
      case MessageConfirmed(deliveryId) =>
        log.info("Confirmed {}", deliveryId)
        confirmDelivery(deliveryId)
    }
  }

  object MyDestination {
    def props(reliability: Int = 100): Props = Props(new MyDestination(reliability))
  }
  class MyDestination(reliability: Int) extends Actor  with ActorLogging {
    require(reliability >= 0 && reliability <= 100)
    override def receive: Receive = {
      case msg @ Message(deliveryId, value) =>
        log.info("receive({})", msg)
        if (Random.nextInt(100) < reliability) {
          log.info("confirm({})", deliveryId)
          sender() ! Confirm(deliveryId)
        } else {
          log.info("Ignore({})", deliveryId)
        }
    }
  }
}