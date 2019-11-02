package com.company.iris.messages

import akka.actor.ActorRef


trait WsMessageUp {
  val content: String
}

case class  WsTextUp(content : String)      extends WsMessageUp
case class  WsBinaryUp(content : String)    extends WsMessageUp



trait       WsMessageDown{val content : String}


case class  WsTextDown(content : String)   extends WsMessageDown
case class  WsBinaryDown(content : String) extends WsMessageDown


case class  UserOffline(userId: String,    content: String = "user_offline")    extends WsMessageDown
case class  UserOnline(actorRef: ActorRef, content: String = "user_online")     extends WsMessageDown


object IrisMessages {

  val upMessageToDown: WsMessageUp => WsMessageDown = (message) => IrisMessages.inMessageToOut(message)

  def inMessageToOut(up: WsMessageUp): WsMessageDown ={
    up match {
      case up : WsTextUp   => inMessageToOut(up)
      case up : WsBinaryUp => inMessageToOut(up)
    }
  }

  def inMessageToOut(textMessageUp: WsTextUp): WsTextDown = {
     WsTextDown(textMessageUp.content)
  }

  def inMessageToOut(binaryUp: WsBinaryUp): WsBinaryDown = {
    WsBinaryDown(binaryUp.content)
  }



}



