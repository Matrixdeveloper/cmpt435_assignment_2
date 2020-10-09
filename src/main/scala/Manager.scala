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
    case a:StartMessage => buildFrame(a)
    case "Request" => respond()
    case ("Report",b:Array[Array[Double]]) =>
      context.parent ! EndMessage(this.outputPath, b.length, b)
      context.stop(self)

    case _=> print("Manager: unexpected message from"+ context.sender)
  }

  def buildFrame(m:StartMessage): Unit =
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
        Array.ofDim(m.numBody,3))), "worker"+i.toString)

    // activate worker
    val wArray = worker.toArray
    worker.foreach(w=>w ! ("Start", wArray))
    println("Manager: Frame Set Up")
  }

  def respond(): Unit =
  {
    workerFree+=1
    // wait for remaining work to be done
    if(numDT>=1 && BagIter<Bags.length){
      sender ! Bags(BagIter)
      BagIter+=1;workerFree-=1
    }else if(numDT>1 && BagIter==Bags.length){
      println("Manager: Next interval")
      workerFree-=1
      BagIter = 0;numDT -= 1
      sender ! Bags(BagIter)
    }else if(numDT == 1 && BagIter==Bags.length){
      if(workerFree<numWorker) sender ! "Done"
      else sender ! "Report"
    }
  }
}



