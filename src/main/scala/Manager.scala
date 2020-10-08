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

class Manager(val outputPath:String, val numWorker:Int, var workerFree:Int,
             var Bags:Array[(Int,Int)], var BagIter: Int, var numDT:Int)
  extends Actor
{
  override def receive: Receive = {
    case a:StartMessage => buildFrame(a)
    case "Request" => respond()
    case _=> print("Manager: unexpected message from"+ context.sender)
  }

  def buildFrame(m:StartMessage): Unit =
  {
    // create task basket and initialize bag index
    this.Bags =
      (for(i<-0 until m.numBody/m.numWorker;
           j<-0 until m.numBody/m.numWorker) yield(i,j))
      .filter(p=>p._2>=p._1).toArray

    this.BagIter = 0

    // create workers
    val worker=for(i<- 0 until this.numWorker)
      yield context
        .actorOf(Props(new Worker(m.data,null,i)), "worker" +i.toString)

    // activate worker
    val wArray = worker.toArray
    worker.foreach(w=>w ! ("Start", wArray))
    println("Manager: Frame Set Up")
  }

  def respond(): Unit =
  {
    workerFree+=1
    // determine if all work have done
    if(this.numDT==0 && this.workerFree==this.numWorker) {
      implicit val timeout: Timeout = Timeout(5.seconds)
      val future = context.sender ? "Report"
      val result = Await
        .result(future, timeout.duration)
        .asInstanceOf[Array[Array[Double]]]
      context.parent ! EndMessage(this.outputPath, result.length, result)
      context.stop(self)
    }

    // wait for remaining work to be done
    if(this.numDT==0 && this.BagIter==this.Bags.length){sender ! "Done";return}

    // distribute bag
    if(numDT>0 && this.BagIter==this.Bags.length){
      this.BagIter = 0;this.numDT-=1;println("Manager: Next interval")
    }
    sender ! this.Bags(this.BagIter)
    this.BagIter+=1
    this.workerFree-=1
  }
}



