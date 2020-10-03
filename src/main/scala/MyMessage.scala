//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

/**
 * Base class of my messages
 */
abstract class MyMessage {
  def sender: String
  def recipient: String

}

/**
 * Define the initial message format
 * @param sender
 * @param recipient
 * @param inputPath
 * @param outputPath
 * @param numDeltaTime
 * @param numWorker
 * @param DeltaTime
 */
case class InitMessage(sender: String, recipient: String,
                      inputPath: String, outputPath: String,
                      numDeltaTime: Int, numWorker: Int,
                      DeltaTime: Double) extends MyMessage

/**
 * Define the message of manager send to worker
 * @param sender
 * @param recipient
 * @param taskID
 */
case class TaskMessage(sender: String, recipient: String,
                      taskID: Int, report: Boolean) extends MyMessage

/**
 * Define peer messages between workers
 * @param sender
 * @param recipient
 * @param bodyID
 */
case class BodyMessage(sender: String, recipient: String,
                      bodyID: Int) extends MyMessage

/**
 * Define report format of worker
 * @param sender
 * @param recipient
 * @param curPostData
 * @param curVelocityData
 */
case class ReportMessage(sender: String, recipient: String,
                        curPostData:Array[Array[Double]],
                        curVelocityData:Array[Array[Double]]) extends MyMessage

case class EndMessage()
