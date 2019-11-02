package com.company.iris.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.company.iris.chat.session.ChatSession

import scala.concurrent.{ExecutionContext, Future}

object Routes {

  def logRoute(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, notificationActor: ActorRef)
  =logDuration(routeRoot)


  //log duration and request info route
  def logDuration(inner: Route)(implicit ec: ExecutionContext): Route = {
    inner
  }


  def routeRoot(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer, notificationActor: ActorRef)
  = {
      routeTable ~
      extractRequest { request =>
        badRequest(request)
      }
    }

  def badRequest(request: HttpRequest): StandardRoute = {
    val method = request.method.value.toLowerCase
    method match {
      case _ =>
        complete((StatusCodes.NotFound, "404 error, resource not found!"))
    }
  }

  def routeTable(implicit ec: ExecutionContext, system: ActorSystem,
                 materializer: ActorMaterializer, notificationActor: ActorRef)= {
    routeHelloWorld ~
    routeRegisterUser ~
    routeGetUsers ~
    routeWebsocket
  }


  def routeWebsocket(implicit ec: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer) = {
    get {
      //use for chat service
      path("chat") {
        val chatSession = new ChatSession()
        handleWebSocketMessages(chatSession.chatService)
      }
    }
  }



  def routeHelloWorld() ={
      get {
        path("hello") {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  def routeRegisterUser(implicit  executionContext: ExecutionContext) = {
    post{
      path("api" / "registerUser"){
        complete(StatusCodes.OK)
      }
    }
  }

  def routeGetUsers(implicit executionContext: ExecutionContext) = {
      get{
        path("api"/ "getUsers"){
          complete(HttpEntity(ContentTypes.`application/json`, mockGetUsers()))
        }
      }
  }

  def mockGetUsers() = {
    """{
         "Users": [
             {
               "uid": "id_1",
               "isOnline": 1,
               "data": {
                    "firstName": "Jonathan",
                    "lastName" : "Tadros"
                  }
             },
             {
              "uid": "id_2"
              "isOnline": 1,
              "data": {
                  "firstName": "Gus",
                  "lastName" : "E"
              }
            }
         ]
       }
    """
  }

}
