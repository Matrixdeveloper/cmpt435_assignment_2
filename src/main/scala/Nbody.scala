//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import java.io.{File, PrintWriter}
import scala.io.Source
import akka.actor._

/**
 * >> Nbody
 * Main class of 3-D Nbody simulation program
 * This class has two responsibility:
 *  1. read data from file & output simulation result to file
 *  2. create a manager to manage simulation task
 *
 * Required value for activating :
 *  1. input file path
 *  2. output file path
 *  3. number of worker
 *  4. number of intervals
 *  5. length of single interval
 */
class Nbody extends Actor {
  override def receive: Receive = {
    case a:InitMSG  => init(a)
    case b:EndMSG   => finish(b);
    case _          => println("Nbody: unexpected message")
  }

  /**
   * responsible for read data and create worker
   * @param msg InitMSG, check MyMessage
   */

  def init(msg:InitMSG): Unit ={
    println("Initializer Start")
    val source = Source.fromFile(msg.inputPath)
    var rawList:List[String] = source.getLines().toList
    source.close()
    // extract first line <number of body> from source data
    val numBody = rawList.head.toInt;rawList = rawList.tail
    val bodyData: Array[Array[Double]] = rawList.toArray.map(
      _.split(" ").map(_.toDouble))

    // precondition of textbook
    if(numBody % msg.numWorker != 0 || numBody/msg.numWorker<1){
      println("Error: expect body number is a multiple of worker number\n")
      context.system.terminate()
    }

    // create manager
    val manager = context.actorOf(Props(
      new Manager(msg.outputPath,msg.numWorker,numBody,
        0,null,0,msg.numDeltaTime)), "Manager")
    // activate manager
    manager ! StartMSG(bodyData, msg.DeltaTime)
  }

  /**
   * for ending the program, output data
   * @param msg EndMSG, check MyMessage
   */

  def finish(msg:EndMSG): Unit ={
    val file = new PrintWriter(
      new File(msg.outputPath))
    // first line is body number
    file.write(msg.numBody.toString+"\n")
    // counter for avoid extra newline after last line
    var x = 0
    msg.finalResult.foreach(row=>
      if(x<msg.numBody-1){file.write(row.mkString(" ")+"\n");x+=1}
      else file.write(row.mkString(" ")))
    file.close()
    println("System Exit")
    context.system.terminate()
  }
}

/**
 *  booting program
 */

object NbodyMain {
  def main(args: Array[String]): Unit = {
    // command-line support
    if (args.length>0 && args.length<5){
      println("Too few arguments: expect 0 or 5 arguments")
      return
    } else if (args.length>5){
      println("Too much arguments: expect 0 or 5 arguments")
      return
    }

    var inputFile:String = ""
    var outputFile:String = ""
    var numWorker:Int = 0
    var numInterval:Int = 0
    var interval:Double = 0.0

    // if no argument provide, using default argument and input file
    if(args.length == 0){
      inputFile = "myInput.txt";outputFile = "sampleOutput.txt"
      interval = 1.0;numInterval = 2; numWorker = 4
    }else{
      // args.length == 5
      // validating input arguments
      try{
        // input file must exist
        if(!new File(args(0)).isFile)
          throw new Exception("Error:Input file not exists")
        else inputFile = args(0)

        outputFile = args(1)

        if(args(2).toInt<=0)
          throw new Exception("Error:worker number must be positive integer")
        else if((args(2).toInt+1)/2 < 2) {
          // textbook made this assumption/precondition
          throw new Exception("Error: too less tasks; expected task number " +
            "at least twice as many task" +
            "as workers\n >>> (number of worker+1)/2 >=2")
        } else numWorker = args(2).toInt

        if(args(3).toInt<=0)
          throw new Exception("Error:number of interval must be positive integer")
        else numInterval = args(3).toInt

        if(args(4).toDouble<0)
          throw new Exception("Error:interval length cannot be negative")
        else interval = args(4).toDouble
      }
      catch {
        case e:Exception=> println(e)
          return
      }
    }

    printf(">>>>Argument list<<<<\ninput:%s" +
      "\noutput:%s\nworker number:%d\ninterval number:%d" +
      "\ninterval length:%f\n\n\n",
      inputFile, outputFile, numWorker, numInterval,interval)

    // create system
    val actorSystem: ActorSystem = ActorSystem("NbodySystem")
    // create initializer
    val firstActor: ActorRef = actorSystem.actorOf(Props[Nbody](), "Initializer")
    // activate initializer
    firstActor ! InitMSG(inputFile, outputFile, numInterval, interval,
      numWorker)
  }
}