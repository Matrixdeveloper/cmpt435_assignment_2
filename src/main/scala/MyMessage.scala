import akka.actor.ActorRef
//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857

trait MyMessage

// Send by user
/**
 *  for initialize program
 * @param inputPath, String, file path which contains Nbody data
 * @param outputPath, String, file path which will store simulation result
 * @param numDeltaTime Int, number of intervals of simulation
 * @param DeltaTime Double, length of each interval
 * @param numWorker Int, number of worker to be created
 */
case class InitMSG(inputPath: String, outputPath: String,
                   numDeltaTime: Int, DeltaTime: Double,
                   numWorker: Int) extends MyMessage

// Send by initializer
/**
 * for passing essential data to manager
 * @param mpvdata, Array[Array[Double], contains all bodies' data
 *  each subarray length == 7, 1 for mess, 3 for position, 3 for velocity
 * @param interval, Double, length of each interval
 */
case class StartMSG(mpvdata:Array[Array[Double]], interval:Double) extends MyMessage

// Send by manager
/**
 * for initialize worker
 * @param peerWorkers, Array[ActorRef],since workers are created dynamically,
 * the peers reference must be passed after all worker are created
 */
case class InitWorkerMSG(peerWorkers: Array[ActorRef]) extends MyMessage

/**
 * for passing task pair to worker
 * @param block_pair, (Int,Int), first int represent block1, second int represent block2
 */
case class BlockMSG(block_pair:(Int,Int)) extends MyMessage

/**
 * for telling worker report the nbody data it holds
 */
case class AskWorkerReportMSG() extends MyMessage

/**
 * for telling worker terminate itself
 */
case class TerminateWorkerMSG() extends MyMessage

/**
 * for returning simulation result to initializer
 * @param outputPath  output file path
 * @param numBody number of bodies in that simulation
 * @param finalResult simulation result of bodies data
 */
case class EndMSG(outputPath:String,numBody:Int,finalResult:Array[Array[Double]]) extends MyMessage



// Send by worker
/**
 * for request task from manager
 */
case class RequestBlocksMSG()extends MyMessage

/**
 * for broadcast temporary force data with peers
 * @param temp_force contains accumulated force data which calculated by this worker
 */
case class ExchangeForceMSG(temp_force: Array[Array[Double]]) extends MyMessage

/**
 * for broadcast temporary moves with peers
 * @param blockHead the first body index which belongs to this worker's block
 * @param blockTail the end index of body belong to this block
 * @param temp_move temporary moves which calculated by this worker
 */
case class ExchangeMoveMSG(blockHead:Int,blockTail:Int, temp_move: Array[Array[Double]]) extends MyMessage

/**
 * for return final simulation data to manager
 * @param final_body_data format same as input data
 */
case class WorkerReportMSG(final_body_data: Array[Array[Double]]) extends MyMessage

