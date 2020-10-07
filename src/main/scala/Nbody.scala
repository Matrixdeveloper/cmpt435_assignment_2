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
    case a:InitMessage => init(a)
    case b:EndMessage  => finish(b);
    case _ => println("Nbody: unexpected message")
  }

  def init(msg:InitMessage): Unit ={
    println("Initializer Start")
    val source = Source.fromFile(System.getProperty("user.dir")+
      "/src/main/scala/"+msg.inputPath)
    var rawList:List[String] = source.getLines().toList
    source.close()

    val numBody = rawList.head.toInt;rawList = rawList.tail
    val bodyData: Array[Array[Double]] = rawList.toArray.map(
      _.split(" ").map(_.toDouble))

    val manager = context.actorOf(Props(new Manager(msg.outputPath,
      msg.numWorker,0)), "Manager")
    manager ! StartMessage(msg.numWorker, 0, numBody, msg.numDeltaTime,
      msg.DeltaTime, bodyData)
  }

  def finish(msg:EndMessage): Unit ={
    val file = new PrintWriter(new File(System.getProperty("user.dir")+"/src" +
      "/main/scala/"+msg.outputPath))
    file.write(msg.numBody.toString+"\n")
    var x = 0
    msg.finalResult.foreach(row=>
      if(x<msg.numBody-1){file.write(row.mkString(" ")+"\n");x+=1}
      else file.write(row.mkString(" ")))
    file.close()
    println("Initializer End")
    context.system.terminate()
  }
}

object MyTest extends App {
  val inputFile = "myInput.txt";val outputFile = "sampleOutput.txt"
  val interval = 1.0;val numInterval = 5; val numWorker = 4
  val actorSystem: ActorSystem = ActorSystem("NbodySystem")
  val firstActor: ActorRef = actorSystem.actorOf(Props[Nbody], "Initializer")
  firstActor ! InitMessage(inputFile, outputFile, numInterval, interval,
    numWorker)
}



