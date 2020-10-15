import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike


class MyManagerTest
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Manager actor" should{

    "respond Block request with Block pair" in {
      val manager = system.actorOf(Props(new Manager("fake", 1,
        2, 0,
        Array((1, 1), (-1, -1)), 0, null)))

      val sender = TestProbe()
      sender.send(manager, RequestBlocksMSG)
      sender.expectMsg(BlockMSG(1, 1))
      sender.send(manager, RequestBlocksMSG)
      sender.expectMsg(BlockMSG(-1, -1))

      manager ! PoisonPill
    }

    "start next interval when all worker are requesting" in {
      val manager = system.actorOf(Props(new Manager("fake", 2,
        2, 0,
        Array((1, 1), (-1, -1)), 0, null)))

      val sender = TestProbe()
      sender.send(manager, WaitNextIntervalMSG)
      sender.send(manager, WaitNextIntervalMSG)
      sender.expectMsg(BlockMSG(1, 1))
      sender.expectMsg(BlockMSG(-1, -1))

      manager ! PoisonPill
    }



    "tell worker to leave if they finish their job" in {
      val manager = system.actorOf(Props(new Manager("fake", 2,
        2, 0,
        Array((1, 1), (-1, -1)), 0, null)))

      val sender = TestProbe()
      sender.send(manager, AllFinishMSG)
      sender.expectMsg(TerminateWorkerMSG)
      sender.send(manager, AllFinishMSG)
      sender.expectMsg(AskWorkerReportMSG)

      manager ! PoisonPill
    }
  }
}
