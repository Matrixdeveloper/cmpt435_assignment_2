import akka.actor.ActorRef
//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

trait MyMessage

// Belong to initializer
case class InitMSG(inputPath: String, outputPath: String,
                       numDeltaTime: Int, DeltaTime: Double,
                       numWorker: Int)

case class StartMSG(numWorker: Int, freeWorker:Int, numBody:Int,
                        numInterval:Int, Interval:Double,
                        data:Array[Array[Double]])

case class EndMSG(outputPath:String,numBody:Int,finalResult:Array[Array[Double]])

// Send by manager
case class InitWorkerMSG(peerWorkers: Array[ActorRef]) extends MyMessage
case class BlockMSG(block_pair:(Int,Int)) extends MyMessage
case class AskWorkerReportMSG() extends MyMessage
case class TerminateWorkerMSG() extends MyMessage
case class ReportMessage(posData:Array[Array[Double]]) extends MyMessage


// Send by worker
case class RequestBlocksMSG()extends MyMessage
case class ExchangeForceMSG(temp_force: Array[Array[Double]]) extends MyMessage
case class ExchangeMoveMSG(temp_move: Array[Array[Double]]) extends MyMessage
case class WorkerReportMSG(final_body_data:Array[Array[Double]]) extends MyMessage

