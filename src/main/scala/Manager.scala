//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

import akka.actor._

class Manager(val outputPath:String, val numWorker:Int, var workerFree:Int,
             var Bags:Array[(Int,Int)], var BagIter: Int, var numDT:Int)
  extends Actor
{
  override def receive: Receive = {
    case a:StartMSG => buildFrame(a)
    case RequestBlocksMSG => respond()
    case WorkerReportMSG(b) =>
      context.parent ! EndMSG(this.outputPath, b.length, b)
      context.stop(self)

    case _=> print("Manager: unexpected message from"+ context.sender)
  }

  def buildFrame(m:StartMSG): Unit =
  {
    // create task basket and initialize bag index
    this.Bags = (for(i<-0 until m.numWorker;j<-0 until m.numWorker)
        yield(i,j)).filter(p=>p._2>=p._1).toArray

    // add sentinel indicate task of current interval have done
    val sentinel = (for(_<- 1 to m.numWorker)yield (-1,-1)).toArray
    this.Bags = this.Bags.concat(sentinel)
    this.BagIter = 0

    // create workers
    val worker=for(i<- 0 until this.numWorker)
      yield context.actorOf(Props(new Worker(m.data, null, i,numWorker-1, 100,
        Array.ofDim(m.numBody,3),Array.ofDim(m.numBody,7), m.Interval)), "worker"+i.toString)

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



