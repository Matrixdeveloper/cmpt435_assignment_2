//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import java.io.{File, PrintWriter}
import scala.io.Source
import akka.actor._

class Nbody extends Actor {
  override def receive: Receive = {
    case a:InitMSG  => init(a)
    case b:EndMSG   => finish(b);
    case _          => println("Nbody: unexpected message")
  }

  def init(msg:InitMSG): Unit ={
    println("Initializer Start")
    val source = Source.fromFile(msg.inputPath)
    var rawList:List[String] = source.getLines().toList
    source.close()

    val numBody = rawList.head.toInt;rawList = rawList.tail
    val bodyData: Array[Array[Double]] = rawList.toArray.map(
      _.split(" ").map(_.toDouble))

    if(numBody % msg.numWorker != 0 || numBody/msg.numWorker<1){
      println("Error: expect body number is a multiple of worker number\n")
      context.system.terminate()
    }

    val manager = context.actorOf(Props(
      new Manager(msg.outputPath,msg.numWorker,numBody,
        0,null,0,msg.numDeltaTime)), "Manager")
    manager ! StartMSG(bodyData, msg.DeltaTime)
  }

  def finish(msg:EndMSG): Unit ={
    val file = new PrintWriter(
      new File(msg.outputPath))
    file.write(msg.numBody.toString+"\n")
    var x = 0
    msg.finalResult.foreach(row=>
      if(x<msg.numBody-1){file.write(row.mkString(" ")+"\n");x+=1}
      else file.write(row.mkString(" ")))
    file.close()
    println("System Exit")
    context.system.terminate()
  }
}

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


    if(args.length == 0){
      inputFile = "myInput.txt";outputFile = "sampleOutput.txt"
      interval = 1.0;numInterval = 2; numWorker = 4
    }else{// args.length == 5
      try{
        if(!new File(args(0)).isFile)
          throw new Exception("Error:Input file not exists")
        else inputFile = args(0)

        outputFile = args(1)

        if(args(2).toInt<=0)
          throw new Exception("Error:worker number must be positive integer")
        else if((args(2).toInt+1)/2 < 2)
          throw new Exception("Error: too less tasks; expected task number " +
            "at least twice as many task" +
            "as workers\n >>> (number of worker+1)/2 >=2")
        else numWorker = args(2).toInt

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


    val actorSystem: ActorSystem = ActorSystem("NbodySystem")
    val firstActor: ActorRef = actorSystem.actorOf(Props[Nbody], "Initializer")
    firstActor ! InitMSG(inputFile, outputFile, numInterval, interval,
      numWorker)
  }
}






