//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

import akka.actor._


/**
 * create worker, manager worker, and collect data from worker
 *
 * @param outputPath save the output file path from initializer
 * @param numWorker save the worker number
 * @param numBody save the body number
 * @param workerFree dynamically record the current free worker number
 * @param Bags save the generated tasks
 * @param BagIter index of tasks in bag
 */
class Manager(val outputPath:String, val numWorker:Int, val numBody:Int,
               var workerFree:Int, var Bags:Array[(Int,Int)],
               var BagIter: Int, var workers:List[ActorRef]) extends Actor
{
  override def receive: Receive =
  {
    case a:StartMSG => buildFrame(a)
    case RequestBlocksMSG => respond()
    case WaitNextIntervalMSG =>respond2()
    case AllFinishMSG =>respond3()
    case WorkerReportMSG(final_mpvData) =>
      context.parent ! EndMSG(this.outputPath,numBody, final_mpvData)
      context.stop(self)
    case _=> print("Manager: unexpected message from"+ context.sender())
  }

  /**
   * create workers, generate task bag
   * @param initMsg StartMSG, check MyMessage
   */
  def buildFrame(initMsg:StartMSG): Unit =
  {
    println("Manager2")
    // create task basket and initialize bag index
    Bags = (for(i<-0 until numWorker; j<-0 until numWorker)
      yield(i,j)).filter(p=>p._2>=p._1).toArray

    // add sentinel indicate task of current interval have done
    val sentinel = (for(_<- 1 to numWorker)yield (-1,-1)).toArray
    Bags = Bags.concat(sentinel)
    BagIter = 0

    // create workers
    val gravity = 6.67e-11
    val dimension = 3
    val row_length = 7
    // generate worker and record reference to an array
    val worker = for(workerID<-0 until numWorker)
      yield context.actorOf(Props(
        new Worker(workerID, //id of each worker
          numWorker, //number of workers
          initMsg.interval, // length of each interval
          numBody, // number of bodies
          gravity, // gravity constant
          initMsg.mpvdata, // initial body data from input file
          Array.ofDim(numBody,row_length), // temp mpv data for apply moves
          Array.ofDim(numBody,dimension), // temp force from this worker
          Array.ofDim(numBody,dimension), // temp force other worker
          null,
          initMsg.numInterval,
          numWorker,numWorker)),
        "worker"+workerID.toString)

    // activate worker
    val wArray = worker.toArray
    println("Manager: Frame Set Up")
    worker.foreach(w=>w ! InitWorkerMSG(wArray))
  }


  /**
   * react to RequireBlockMSG
   * distributing tasks to workers
   */
  def respond(): Unit = {
    sender() ! BlockMSG(Bags(BagIter))
    BagIter+=1
  }

  /**
   * react to WaitNextIntervalMSG
   * -collect free workers until all worker finish current interval
   * -distribute tasks in arriving order for new interval
   */
  def respond2(): Unit = {
    workerFree += 1
    if(workerFree == 1){
      workers = List(sender())
    }else{
      workers = workers.appended(sender())
    }

    if(workerFree==numWorker){
      workerFree = 0
      BagIter = 0
      println("Manager: next interval")
      workers.foreach(w=>{
        w ! BlockMSG(Bags(BagIter))
        BagIter+=1
      })
    }
  }

  /**
   * react to AllFinishMSG
   * -tell last worker to report its data
   */
  def respond3(): Unit ={
    if(workerFree<numWorker-1){
      workerFree+=1
      sender() ! TerminateWorkerMSG
    }else{
      sender() ! AskWorkerReportMSG
    }
  }
}