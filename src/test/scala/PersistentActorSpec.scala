import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class PersistentActorSpec(system: ActorSystem)
  extends TestKit(system)
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ImplicitSender
    with PersistenceCleanupSupport
    with ActorKillerSupport
{
  override def beforeAll(): Unit = {
    deleteStorageFiles()
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    deleteStorageFiles()
  }
}
