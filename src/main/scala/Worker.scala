//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.{Actor, ActorRef}


class Worker(var posData:Array[Array[Double]], var peers:Array[ActorRef],
             val myId:Int) extends Actor
{
  override def receive: Receive = {
    case TaskMessage =>
      print("Worker")

    case "Report" =>
      context.sender ! posData

    case "Done" =>
      println(self.path.name+">>leave")
      context.stop(self)

    case ("Start", workerRefs:Array[ActorRef]) =>
      peers = workerRefs
      println(self.path.name+">>ready")
      context.parent ! "Request"
  }
}
