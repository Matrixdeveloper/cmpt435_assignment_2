//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor._

/**
 * The base class of Nbody program.
 * It is responsible for initialising program.
 */

class Nbody extends Actor {

  override def receive: Receive = {
    case a:InitMessage =>
      print("manager: Receive Init Message");
      initNbodySystem(a)

    case a:EndMessage =>
      print("manager: Receive result")

    /**
     * write out data and terminate system
     */
      context.system.terminate();
    case _ =>
      print("Nbody: unexpected message")
  }

  def initNbodySystem(msg:InitMessage): Unit ={

    /**
     * Read input data
     * create workers and initialize with input data
     * send tasks to workers && scheduling/balancing workload
     * keep working
     * wait for last task finish
     * ask result from last worker
     * Write result to output
     */
  }

  def writeOut(msg:EndMessage): Unit ={

  }

}


object MyTest extends App {
  val actorSystem: ActorSystem = ActorSystem("NbodySystem")
  val firstActor: ActorRef = actorSystem.actorOf(Props[Nbody], "InitActor")

  firstActor ! EndMessage()


}



