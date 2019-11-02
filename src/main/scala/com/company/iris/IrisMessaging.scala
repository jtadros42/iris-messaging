package com.company.iris

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.stream.ActorMaterializer
import com.company.iris.chat.NotificationActor
import com.company.iris.cli.CommandLineOptions
import com.company.iris.routes.Routes

object IrisMessaging extends App {
  import system.dispatcher

  val serverContext = ConnectionContext.noEncryption()

  // Command Line Options
  val parsedArgs    = CommandLineOptions.parseArgs(args)
  val webPort       = parsedArgs.getWebPort()
  val akkaPort      = parsedArgs.getAkkaPort()
  val seedNodes     = parsedArgs.getSeedNodes()

  implicit val system            = ActorSystem("chat-cluster")
  implicit val materializer      = ActorMaterializer()
  implicit val notificationActor = system.actorOf(Props(classOf[NotificationActor]))

  Http().bindAndHandle(Routes.logRoute, "0.0.0.0", webPort, connectionContext = serverContext)

}
