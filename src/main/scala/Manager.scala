//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.Actor

class Manager(val outputPath:String, val numWorker:Int, var workerFree:Int)
  extends Actor
{
  override def receive: Receive = {
    case a:StartMessage =>
      println("Manager: Hello!")
      context.sender() ! EndMessage(outputPath, a.numBody, a.data)

    case a : TaskMessage =>
      print("Manager: a worker is free")

    case _=>
    print("Manager: unexpected message")
  }
}
