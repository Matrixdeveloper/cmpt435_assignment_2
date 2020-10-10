//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.{Actor, ActorRef}


class Worker(var mpvData:Array[Array[Double]], var peers:Array[ActorRef],
             val myId:Int, var waitMsgNum:Int, val G:Double,
             var tF:Array[Array[Double]], var tMPV:Array[Array[Double]],
             val DT:Double,val numBody:Int, val peerNum:Int)extends Actor
{
  def receive: Receive =
  {
    case BlockMSG(block_pair)             => calculateForce(block_pair)
    case ExchangeForceMSG(temp_forces)    => exchangeForce(temp_forces)
    case ExchangeMoveMSG(h,t,temp_moves)  => exchangeMove(h,t,temp_moves)
    case AskWorkerReportMSG               => leave(false)
    case TerminateWorkerMSG               => leave(true)
    case InitWorkerMSG(peer_workers)      => init(peer_workers)
  }

  def init(allPeers: Array[ActorRef]): Unit =
  {
    peers = allPeers
    println(self.path.name+" >> ready")
    context.parent ! RequestBlocksMSG
  }

  def leave(toLeave: Boolean): Unit =
  {
    if(!toLeave) sender() ! WorkerReportMSG(mpvData)
    println(self.path.name+">>leave")
    context.stop(self)
  }

  def calculateForce(t:(Int,Int)): Unit =
  {
    // once receive sentinel bag, exchange forces
    if(t==(-1,-1)){
      0 until peerNum foreach(i=>
        if(i!=myId) peers(i) ! ExchangeForceMSG(tF))
      return
    }
    // generate body pairs from block pair
    val bSize = numBody/peerNum
    val realTask = (
      for(i<-(t._1*bSize) until(t._1*bSize+bSize);
          j<-(t._2*bSize) until(t._2*bSize+bSize))
        yield(i,j)).filter(v=>v._1<v._2)

    // calculate force on generated body pairs
    realTask.foreach(twoBody=>{
      val p1=mpvData(twoBody._1)
      val p2=mpvData(twoBody._2)
      // calculate distance
      val d = Math.sqrt(Math.pow(p1(1)-p2(1), 2)
        +Math.pow(p1(2)-p2(2), 2)
        +Math.pow(p1(3)-p2(3), 2))
      // calculate magnitude
      val m = (G*p1(0)*p2(0))/Math.pow(d,2)
      // calculate direction
      val dd = (p1(1)-p2(1), p1(2)-p2(2), p1(3)-p2(3))
      // accumulate result on my temp_force record
      tF(twoBody._1)(0)=tF(twoBody._1)(0)+m*dd._1/d
      tF(twoBody._2)(0)=tF(twoBody._2)(0)-m*dd._1/d
      tF(twoBody._1)(1)=tF(twoBody._1)(1)+m*dd._2/d
      tF(twoBody._2)(1)=tF(twoBody._2)(1)-m*dd._2/d
      tF(twoBody._1)(2)=tF(twoBody._1)(2)+m*dd._3/d
      tF(twoBody._2)(2)=tF(twoBody._2)(2)+m*dd._3/d
    })
    // request next bag
    context.parent ! RequestBlocksMSG
  }

  def exchangeForce(nf:Array[Array[Double]]): Unit =
  {
    // count unreceived exchange msg
    waitMsgNum-=1
    // accumulate new force on temp force record
    tF = (for(i<-0 until numBody) yield
      Array(
        tF(i)(0)+nf(i)(0),
        tF(i)(1)+nf(i)(1),
        tF(i)(2)+nf(i)(2))
      ).toArray
    // once receive all forces
    if(waitMsgNum==0){
      waitMsgNum = peerNum-1
      println(self.path.name+">>>Successfully Exchange Force")
      calculateMoves()
    }
  }

  def calculateMoves(): Unit =
  {
    val firstIndex = myId * (numBody / peerNum)
    val lastIndex = firstIndex + (numBody / peerNum)

    firstIndex until lastIndex foreach(i=>{
      val m = mpvData(i)(0)
      val f = tF(i)
      val dv =Array(f(0)/m*DT,f(1)/m*DT,f(2)/m*DT)
      val vx = mpvData(i)(4)
      val vy = mpvData(i)(5)
      val vz = mpvData(i)(6)
      val dp =Array((vx+dv(0)/2)*DT,(vy+dv(1)/2)*DT,(vz+dv(2)/2)*DT)
      tMPV(i)=0.0+:dp.concat(dv)
    })

    0 until peerNum foreach(i=>
      if(i!=myId) peers(i) ! ExchangeMoveMSG(firstIndex,lastIndex,tMPV))
  }

  def exchangeMove(first:Int, last:Int, nMPV:Array[Array[Double]]): Unit =
  {
    waitMsgNum-=1
    // copy other block moves
    first until last foreach(i=>tMPV(i)=nMPV(i))
    // once receive all moves, update self record
    if(waitMsgNum==0){
      println(self.path.name+">>> Move")
      mpvData = (for(i<-0 until numBody) yield {
        Array(
          mpvData(i)(0)+tMPV(i)(0), mpvData(i)(1)+tMPV(i)(1),
          mpvData(i)(2)+tMPV(i)(2), mpvData(i)(3)+tMPV(i)(3),
          mpvData(i)(4)+tMPV(i)(4), mpvData(i)(5)+tMPV(i)(5),
          mpvData(i)(6)+tMPV(i)(6)
        )}).toArray
      println(self.path.name+"\n"
              +mpvData.map(_.mkString(" ")).mkString("\n")+"\n")
      // refresh temporary value
      waitMsgNum = peerNum-1
      tMPV = Array.ofDim(numBody,7)
      context.parent ! RequestBlocksMSG
    }
  }
}
