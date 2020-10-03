//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857



import akka.actor.Actor

class Manager(val workerList:Array[Worker], val inputPath:String,
              val outputPath:String, var workerDone:Int) extends Actor
{
  override def receive: Receive = {
    case "start" =>
      print("Manager: Hello!")

    /**
     *  distribute task until all task finish
     */

    case a : TaskMessage =>
      print("Manager: a worker is free")

    case _=>
    print("Manager: unexpected message")
  }
}
