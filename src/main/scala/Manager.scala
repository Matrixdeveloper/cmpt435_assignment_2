//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

import scala.concurrent.Await
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

class Manager(val outputPath:String, val numWorker:Int, var workerFree:Int)
  extends Actor
{
  override def receive: Receive = {
    case a:StartMessage => buildFrame(a)
//      context.parent ! EndMessage(outputPath, a.numBody, a.data)
    case "Request" => workerFree+=1;// control()
      println(workerFree)
      if(workerFree==numWorker) {
        implicit val timeout: Timeout = Timeout(5.seconds )
        val future = context.sender ? "Report"
        val result = Await.result(future, timeout.duration).asInstanceOf[Array[Array[Double]]]
        context.parent ! EndMessage(this.outputPath, result.length,result)
        context.stop(self)
      }


    case _=>
    print("Manager: unexpected message from"+ context.sender)
  }

  def buildFrame(m:StartMessage): Unit =
  {
    // create some worker
    var worker=for(i<- 0 until this.numWorker)
      yield context.actorOf(Props(new Worker(m.data,null,i)), "worker"
        +i.toString)

    // activate worker
    val wArray = worker.toArray
    worker.foreach(w=>w ! ("Start", wArray))
    println("Manager: Frame Set Up")
  }

  def control(): Unit =
  {
    context.sender() ! "Done"
//    workerFree-=1
  }

}
