//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.Actor


class Worker(var posData:Array[Array[Double]],
             var velocityData:Array[Array[Double]],
             val peers:Array[Worker], val myId:Int) extends Actor
{
  override def receive: Receive = {
    case TaskMessage =>
      print("Worker");
//    case BodyMessage(_,_,id) =>
//      print("Worker: update %d info", id);

  }
}
