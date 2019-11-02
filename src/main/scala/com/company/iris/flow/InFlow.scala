package com.company.iris.flow

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.company.iris.utils.CommonUtils

import scala.concurrent.ExecutionContext

object InFlow {

  private val parallelism : Int = 6
  private val bufferSize  : Int = 1000


  def parseMessagesFromSocket(message: Message)(implicit executionContext: ExecutionContext,
                                                         materializer: ActorMaterializer) = {
     message match {
       case mess: TextMessage   => mess.textStream.runFold("")(_ + _) .map{
         str => CommonUtils.parseTextMessage(str)
       }
       case mess: BinaryMessage => mess.dataStream.runFold(ByteString.empty)(_ ++ _).map{
         byteString => CommonUtils.parseBinaryMessage(byteString)
       }
     }
  }

  def defineFlowFromWebSocket()(implicit executionContext: ExecutionContext,
                                materializer: ActorMaterializer) = {
    Flow[Message].collect {
      case message: Message => parseMessagesFromSocket(message)
    }.buffer(bufferSize, OverflowStrategy.backpressure).mapAsync(parallelism)(t => t)
  }


}
