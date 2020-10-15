import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike


class MyWorkerTest
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


    "Worker Actor" should {
    "requestBlock with InitWorkerMSG in TestProbe" in {
      val worker = system.actorOf(Props(new Worker(
        0, 0, 0, 0, 0,
        null, null, null, null, null,
        0, 0, 0)))

      worker ! InitWorkerMSG(null)
      expectMsg(RequestBlocksMSG)
      worker ! PoisonPill
    }

   "report body data with AskWorkerReportMSG in TestProbe" in{
     val fakeBody = Array(Array(1.0,1.0,1.0),Array(2.0,2.0,2.0))
     val worker = system.actorOf(Props(new Worker(
       0, 0, 0, 0, 0,
       fakeBody, null, null, null, null,
       0, 0, 0)))

     worker ! AskWorkerReportMSG
     expectMsg(WorkerReportMSG(fakeBody))
     worker ! PoisonPill
   }

   "exchange force when receive sentinel task" in {
     val fakeBody = Array(Array(1.0,1.0,1.0),Array(2.0,2.0,2.0))
     val fakeForce = Array(Array(3.0,3.0,3.0),Array(4.0,4.0,4.0))
     val fakeForce2 = Array(Array(5.0,5.0,5.0),Array(6.0,6.0,6.0))

     val sender = TestProbe()

     val worker = system.actorOf(Props(new Worker(
       0, 0, 0, 0, 0,
       fakeBody, null, fakeForce, fakeForce2, Array(sender.ref),
       0, 0, 0)))

     sender.send(worker, BlockMSG((-1,-1)))
     sender.expectMsg(ExchangeForceMSG(fakeForce))
     worker ! PoisonPill
   }
  }
}
