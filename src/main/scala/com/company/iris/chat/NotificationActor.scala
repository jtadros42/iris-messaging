package com.company.iris.chat

import akka.actor.Actor

class NotificationActor extends Actor {
  override def receive: Receive = {
      case _ => System.out.println("recieved something:" + _)
  }
}
