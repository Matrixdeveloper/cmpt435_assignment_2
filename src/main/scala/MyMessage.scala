//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

/**
 * Base class of my messages
 */
abstract class MyMessage {}


case class InitMessage(inputPath: String, outputPath: String,
                       numDeltaTime: Int, DeltaTime: Double,
                       numWorker: Int) extends MyMessage


case class StartMessage(numWorker: Int, freeWorker:Int, numBody:Int,
                        numInterval:Int, Interval:Double,
                        data:Array[Array[Double]])


case class TaskMessage(taskID: Int, report: Boolean) extends MyMessage

case class BodyMessage(bodyID: Int) extends MyMessage


case class ReportMessage(posData:Array[Array[Double]]) extends MyMessage


case class EndMessage(outputPath:String,numBody:Int,finalResult:Array[Array[Double]])
