//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.{Actor, ActorRef}


class Worker(var posData:Array[Array[Double]], var peers:Array[ActorRef],
             val myId:Int, var waitMsgNum:Int, val G:Double,
             var myF:Array[Array[Double]]) extends Actor
{
  override def receive: Receive = {
    case (a:Int, b:Int) => calculateFore((a,b))

    case ("Exchange",_ ,ef:Array[Array[Double]])=>exchangeForce(ef)

    case "Report" =>
      context.sender ! ("Report", posData)
      println(self.path.name+">>leave")
//      context.stop(self)

    case "Done" =>
      println(self.path.name+">>leave")
//      context.stop(self)

    case ("Start", workerRefs:Array[ActorRef]) =>
      peers = workerRefs
      println(self.path.name+">>ready")
      context.parent ! "Request"
  }

  def calculateFore(t:(Int,Int)): Unit ={
//    println(self.path.name+">>>>>>>>>>"+t.toString())
    if(t==(-1,-1)){
      0 until peers.length foreach(i=>
        if(i!=myId) peers(i) ! ("Exchange","Force", myF))
//      context.parent ! "Request"
      return
    }

    val bSize = posData.length/peers.length
    val realTask = (for(i<-(t._1*bSize) until(t._1*bSize+bSize);
                        j<-(t._2*bSize) until(t._2*bSize+bSize))
      yield(i,j)).filter(v=>v._1<v._2)

    val f = this.myF
    realTask.foreach(pair=>{
      val p1=posData(pair._1)
      val p2=posData(pair._2)
      // calculate distance
      val d = Math.sqrt(Math.pow(p1(1)-p2(1), 2)
        +Math.pow(p1(2)-p2(2), 2)
        +Math.pow(p1(3)-p2(3), 2))
      // calculate magnitude
      val m = (G*p1(0)*p2(0))/Math.pow(d,2)
      // calculate direction
      val dd = (p1(1)-p2(1), p1(2)-p2(2), p1(3)-p2(3))
      // record result
      f(pair._1)(0)=f(pair._1)(0)+m*dd._1/d
      f(pair._2)(0)=f(pair._2)(0)-m*dd._1/d
      f(pair._1)(1)=f(pair._1)(1)+m*dd._2/d
      f(pair._2)(1)=f(pair._2)(1)-m*dd._2/d
      f(pair._1)(2)=f(pair._1)(2)+m*dd._3/d
      f(pair._2)(2)=f(pair._2)(2)+m*dd._3/d})
    this.myF = f
    context.parent ! "Request"
  }

  def exchangeForce(nf:Array[Array[Double]]): Unit ={
    this.waitMsgNum-=1
    myF = (for(i<-0 until posData.length) yield
      Array(myF(i)(0)+nf(i)(0),myF(i)(1)+nf(i)(1), myF(i)(2)+nf(i)(2)))
      .toArray

    if(this.waitMsgNum==0){
      this.waitMsgNum = peers.length-1
      println(self.path.name+">>>Successfully Exchange Force")
      //      println(self.path.name+"\n"
//        +myF.map(_.mkString(" ")).mkString("\n")+"\n")
//      myF = Array.ofDim(posData.length,3)
      context.parent ! "Request"
    }
  }

}
