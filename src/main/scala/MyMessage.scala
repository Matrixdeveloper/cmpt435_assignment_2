import akka.actor.ActorRef
//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

trait MyMessage

// Send by user
case class InitMSG(inputPath: String, outputPath: String,
                   numDeltaTime: Int, DeltaTime: Double,
                   numWorker: Int) extends MyMessage

// Send by initializer
case class StartMSG(mpvdata:Array[Array[Double]], interval:Double) extends MyMessage

// Send by manager
case class InitWorkerMSG(peerWorkers: Array[ActorRef]) extends MyMessage
case class BlockMSG(block_pair:(Int,Int)) extends MyMessage
case class AskWorkerReportMSG() extends MyMessage
case class TerminateWorkerMSG() extends MyMessage
case class EndMSG(outputPath:String,numBody:Int,finalResult:Array[Array[Double]]) extends MyMessage



// Send by worker
case class RequestBlocksMSG()extends MyMessage
case class ExchangeForceMSG(temp_force: Array[Array[Double]]) extends MyMessage
case class ExchangeMoveMSG(blockHead:Int,blockTail:Int, temp_move: Array[Array[Double]]) extends MyMessage
case class WorkerReportMSG(final_body_data: Array[Array[Double]]) extends MyMessage

