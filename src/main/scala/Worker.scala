//CMPT435 2020 Fall term
//Assignment2: N-body
//Name: Yue Weng
//Student number: 1121 9127
//NSID: yuw857


import akka.actor.{Actor, ActorRef}

/**
 *  Responsible for calculate force & move bodies
 *    -request task from manager
 *    -generate body pairs
 *    -calculate forces
 *    -exchange forces
 *    -calculate moves
 *    -exchange move and update body records
 *    -next interval
 * @param mpvData all bodies data, mass, position, velocity
 * @param peers actor reference of all workers
 * @param myId actor id, it is used to avoid send message to self when exchange data
 * @param waitMsgNum record how many message left to receive for current stage
 * @param G record gravity constant
 * @param tF record temporary forces
 * @param tMPV record temporary moves
 * @param DT record interval length
 * @param numBody record number of body
 * @param peerNum record number of worker
 *
 *    Why use redundant variable holds value? Unify value assign process
 *      -every worker use the essential value initialized by manager
 *      -avoid dynamically recalculate value
 *      eg: number of body< array length of mpvdata>
 *
 */
class Worker(val myId:Int, val peerNum:Int,
              val DT:Double,val numBody:Int,val G:Double,
              var mpvData:Array[Array[Double]], var tMPV:Array[Array[Double]],
              var myTF:Array[Array[Double]], var tF:Array[Array[Double]],
              var peers:Array[ActorRef], var numDT:Int,
              var waitMsgNum:Int,var waitMove:Int)
  extends Actor {

  def receive: Receive = {
    case BlockMSG(block_pair)             => calculateForce(block_pair)
    case ExchangeForceMSG(temp_forces)    => exchangeForce(temp_forces)
    case ExchangeMoveMSG(h,t,temp_moves)  => exchangeMove(h,t,temp_moves)
    case AskWorkerReportMSG               => leave(false)
    case TerminateWorkerMSG               => leave(true)
    case InitWorkerMSG(peer_workers)      => init(peer_workers)
    case StartNextMSG() => context.parent ! RequestBlocksMSG
  }

  /**
   * initialize worker and update peers reference
   * @param allPeers array of peer worker reference
   */
  def init(allPeers: Array[ActorRef]): Unit = {
    peers = allPeers
    println(self.path.name+" >> ready")
    numDT-=1
    context.parent ! RequestBlocksMSG
  }

  /**
   * decide how a worker will do when all work has been finished
   * @param toLeave if true, directly leave; otherwise, report body data and then leave
   */
  def leave(toLeave: Boolean): Unit = {
    if(!toLeave) sender() ! WorkerReportMSG(mpvData)
    println(self.path.name+">>leave")
    context.stop(self)
  }

  /**
   * calculate force of argument task(block pair)
   * @param t task/block pair
   */
  def calculateForce(t:(Int,Int)): Unit = {
    // once receive sentinel bag, exchange forces
    if(t==(-1,-1)){
      peers.foreach(w=>{w ! ExchangeForceMSG(myTF)})
      return
    }
    // otherwise calculate forces for the task
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
      myTF(twoBody._1)(0) = myTF(twoBody._1)(0) + m*dd._1/d
      myTF(twoBody._2)(0) = myTF(twoBody._2)(0) - m*dd._1/d
      myTF(twoBody._1)(1) = myTF(twoBody._1)(1) + m*dd._2/d
      myTF(twoBody._2)(1) = myTF(twoBody._2)(1) - m*dd._2/d
      myTF(twoBody._1)(2) = myTF(twoBody._1)(2) + m*dd._3/d
      myTF(twoBody._2)(2) = myTF(twoBody._2)(2) - m*dd._3/d
    })
    context.parent ! RequestBlocksMSG
  }

  /**
   * receive force from peer workers
   * @param nf new force
   */
  def exchangeForce(nf:Array[Array[Double]]): Unit = {
    if(waitMsgNum == 0){
      println("[][]"+self.path.name+"error "+waitMsgNum)
    }
    // count unreceived exchange msg
    waitMsgNum-=1
    tF = (for(i<-0 until numBody) yield
      Array(
        tF(i)(0)+nf(i)(0),
        tF(i)(1)+nf(i)(1),
        tF(i)(2)+nf(i)(2))
      ).toArray
    if(waitMsgNum==0){
      println(self.path.name+">>>Successfully Exchange Force")
      calculateMoves()
    }
  }

  /**
   * calculate moves for body which belongs to this worker's block
   */
  def calculateMoves(): Unit = {
    val firstIndex = myId * (numBody / peerNum)
    val lastIndex = firstIndex + (numBody / peerNum)

    // calculate moves for body belong to my block
    val blockMove =
      (for(i<-firstIndex until lastIndex)
        yield {
          val m = mpvData(i)(0)
          val f = tF(i)
          val dv =Array(f(0)/m*DT,f(1)/m*DT,f(2)/m*DT)
          val vx = mpvData(i)(4)
          val vy = mpvData(i)(5)
          val vz = mpvData(i)(6)
          val dp =Array((vx+dv(0)/2)*DT,(vy+dv(1)/2)*DT,(vz+dv(2)/2)*DT)
          0.0+:dp.concat(dv)
        }).toArray
    //    var blockMove = Array.ofDim[Double](numBody/peerNum, 7)
    // tell other moves in my block
    peers.foreach(w=>{w ! ExchangeMoveMSG(firstIndex,lastIndex,blockMove)})
  }


  /**
   * receive moves from peer worker and apply move to body data
   * @param first index of body number, indicate where the iteration start
   * @param last  index, indicate where the iteration end
   * @param nMPV new moves from other worker
   */
  def exchangeMove(first:Int, last:Int, nMPV:Array[Array[Double]]): Unit = {
    waitMove -=1
    // accumulate move
    var j = 0
    first until last foreach(i=>{tMPV(i)=nMPV(j);j+=1})


    if(waitMove ==0){
      // finish all moves in this interval
      0 until numBody foreach(i=>{
        mpvData(i)(0) = mpvData(i)(0)+tMPV(i)(0)
        mpvData(i)(1) = mpvData(i)(1)+tMPV(i)(1)
        mpvData(i)(2) = mpvData(i)(2)+tMPV(i)(2)
        mpvData(i)(3) = mpvData(i)(3)+tMPV(i)(3)
        mpvData(i)(4) = mpvData(i)(4)+tMPV(i)(4)
        mpvData(i)(5) = mpvData(i)(5)+tMPV(i)(5)
        mpvData(i)(6) = mpvData(i)(6)+tMPV(i)(6)
      })

      // reset state
      waitMsgNum=peerNum
      waitMove = peerNum

      // clear temp data, myTF, tF, tMPV
      0 until numBody foreach(i=>{
        myTF(i) = Array.ofDim[Double](3)
        tF(i) = Array.ofDim[Double](3)
        tMPV(i) = Array.ofDim[Double](7)
      })

      println(self.path.name+"[posdata]\n"
        +mpvData.map(_.mkString(" ")).mkString("\n")+"\n")

      println(self.path.name+">>> Move done")
      if (numDT ==0) context.parent ! AllFinishMSG
      else {numDT-=1;context.parent ! WaitNextIntervalMSG}
    }
  }
}
