package com.company.iris.chat.broker

import akka.actor.{Actor, ActorPath, ActorRef}

import scala.collection.mutable







class ChatMessageBroker extends Actor {

  val userRegistry : mutable.Map[String, ActorRef] = mutable.Map()
  val chatChannels : mutable.Map[String, ActorRef] = mutable.Map()

  override def receive: Receive = {

  }
}
