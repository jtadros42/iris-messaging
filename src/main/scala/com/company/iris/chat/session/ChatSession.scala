package com.company.iris.chat.session

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream._
import akka.{Done, NotUsed}
import com.company.iris.flow.InFlow
import com.company.iris.messages.{EventEnvelope, _}
import com.company.iris.utils.CommonUtils
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}


class ChatSession()(implicit executionContext: ExecutionContext, actorSystem: ActorSystem, materializer: ActorMaterializer) {

  implicit val messages                                      = Json.writes[WsTextDown]
  val chatSessionActor                                       = actorSystem.actorOf(Props(classOf[ChatSessionActor]))
  val source: Source[EventEnvelope[WsMessageDown], ActorRef] = Source.actorRef[EventEnvelope[WsMessageDown]](
                                                                                  bufferSize = Int.MaxValue, OverflowStrategy.fail
                                                                                )


  def chatService: Flow[Message, Strict, ActorRef] = Flow.fromGraph(GraphDSL.create(source) {
    implicit builder =>
                chatSource =>
                  import GraphDSL.Implicits._

                  val flowFromWebSocket : FlowShape[Message, EventEnvelope[WsMessageUp]]   = builder.add(InFlow.defineFlowFromWebSocket())
                  val chatActorSink     : Sink[EventEnvelope[WsMessageDown], NotUsed]      = Sink.actorRef[EventEnvelope[WsMessageDown]](chatSessionActor, UserOffline)

                  val filterSuccess: FlowShape[EventEnvelope[WsMessageUp],
                                               EventEnvelope[WsMessageUp]]                 = builder.add(Flow[EventEnvelope[WsMessageUp]].filter(e => e.isSuccess))

                  val filterFailure: FlowShape[EventEnvelope[WsMessageUp],
                                               EventEnvelope[WsMessageUp]]                 = builder.add(Flow[EventEnvelope[WsMessageUp]].filter(e => e.isFailure))

                  val flowReject:    FlowShape[EventEnvelope[WsMessageUp],
                                               EventEnvelope[WsMessageDown]]               = builder.add(Flow[EventEnvelope[WsMessageUp]].map(m => m.map(e => WsTextDown(e.content))))

                  val logSink:       Sink[EventEnvelope[WsMessageDown], Future[Done]]      = Sink.foreach(m => m.foreach(e => CommonUtils.consoleLog("DEBUG", e.content)))


                  val flowAccept:    FlowShape[EventEnvelope[WsMessageUp],
                                               EventEnvelope[WsMessageDown]]               = builder.add(Flow[EventEnvelope[WsMessageUp]].map(m => m.map(IrisMessages.upMessageToDown)))

                  val connectedWs:   Flow[ActorRef,
                                        EventEnvelope[UserOnline], NotUsed]                = Flow[ActorRef].map[EventEnvelope[UserOnline]](actor => CommonUtils.wrapEnvelope(UserOnline(actor)))

                  val mergeAccept: UniformFanInShape[EventEnvelope[WsMessageDown],
                                                     EventEnvelope[WsMessageDown]]         = builder.add(Merge[EventEnvelope[WsMessageDown]](2))

                  val broadcastWs: UniformFanOutShape[EventEnvelope[WsMessageUp],
                                                      EventEnvelope[WsMessageUp]]          = builder.add(Broadcast[EventEnvelope[WsMessageUp]](2))

                  val createMessage: FlowShape[EventEnvelope[WsMessageDown], Strict]       = builder.add(Flow[EventEnvelope[WsMessageDown]].map(m => TextMessage(CommonUtils.toJson(m))))



                  flowFromWebSocket ~> broadcastWs
                  broadcastWs ~> filterFailure ~> flowReject  ~> logSink              // log in Kafka and merge back to wbs
                  broadcastWs ~> filterSuccess ~> flowAccept  ~> mergeAccept.in(0)   // publishes to kafka  or Cassandra
                  builder.materializedValue    ~> connectedWs ~> mergeAccept.in(1)

                  mergeAccept ~> chatActorSink

                  chatSource  ~> createMessage                    // recieves from kafka or Cassandra
                  FlowShape(flowFromWebSocket.in, createMessage.out)
  })


}

