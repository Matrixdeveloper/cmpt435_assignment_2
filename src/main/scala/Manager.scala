//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

import akka.actor._

class Manager(val outputPath:String, val numWorker:Int, val numBody:Int,
              var workerFree:Int, var Bags:Array[(Int,Int)],
              var BagIter: Int, var numDT:Int) extends Actor
{
  override def receive: Receive = {
    case a:StartMSG => buildFrame(a)
    case RequestBlocksMSG => respond()
    case WorkerReportMSG(final_mpvData) =>
      context.parent ! EndMSG(this.outputPath,numBody, final_mpvData)
      context.stop(self)
    case _=> print("Manager: unexpected message from"+ context.sender)
  }

  def buildFrame(initMsg:StartMSG): Unit =
  {
    // create task basket and initialize bag index
    Bags = (for(i<-0 until numWorker; j<-0 until numWorker)
        yield(i,j)).filter(p=>p._2>=p._1).toArray

    // add sentinel indicate task of current interval have done
    val sentinel = (for(_<- 1 to numWorker)yield (-1,-1)).toArray
    Bags = Bags.concat(sentinel)
    BagIter = 0

    // create workers
    val numWaitPeer = numWorker-1
    val Gravity = 6.67e-11
    val dimension = 3
    val row_length = 7
    val worker=for(workerID<- 0 until numWorker)
      yield context.actorOf(Props(
        new Worker(initMsg.mpvdata, null,
          workerID, numWaitPeer, Gravity,
          Array.ofDim(numBody,dimension),
          Array.ofDim(numBody,row_length),
          initMsg.interval, numBody,numWorker)),
        "worker"+workerID.toString)

    // activate worker
    val wArray = worker.toArray
    worker.foreach(w=>w ! InitWorkerMSG(wArray))
    println("Manager: Frame Set Up")
  }

  def respond(): Unit =
  {
    workerFree+=1
    // wait for remaining work to be done
    if(numDT>=1 && BagIter<Bags.length){
      // bags not all consumed
      sender ! BlockMSG(Bags(BagIter))
      BagIter+=1;workerFree-=1
    }else if(numDT>1 && BagIter==Bags.length){
      // bags all consumed but have more interval to go
      println("Manager: Next interval")
      workerFree-=1
      BagIter = 0;numDT -= 1
      sender ! BlockMSG(Bags(BagIter))
    }else if(numDT == 1 && BagIter==Bags.length){
      // bags and interval both consumed
      if(workerFree<numWorker) sender ! TerminateWorkerMSG
      else sender ! AskWorkerReportMSG
    }else{
      println("Manager: respond error")
    }
  }
}



