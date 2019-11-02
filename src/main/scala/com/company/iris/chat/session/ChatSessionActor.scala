package com.company.iris.chat.session

import akka.actor.{Actor, ActorRef}
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import com.company.iris.messages._
import com.company.iris.utils.CommonUtils



class ChatSessionActor extends Actor {

  val system   = context.system
  val mediator = DistributedPubSub(context.system).mediator


  var streamReference  = ActorRef.noSender
  var userID           =  ""


  def publishToChatRoom(chatId: String, event: SuccessEnvelope[WsMessageDown]) = {
      CommonUtils.consoleLog("DEBUG", "in publish to Mailbox with target: " + chatId)
  }


  def publishToMailbox(target: String, event: SuccessEnvelope[WsMessageDown])  = {
      CommonUtils.consoleLog("DEBUG", "in publish to Mailbox with target: " + target)

  }


  override def receive: Receive = {


    case envelope : SuccessEnvelope[WsMessageDown] => {
               matchEnvelopeContents(envelope, envelope.get)
    }
    case MemberUp(member) =>
                          println(s"*** Member is Up: $self ${member.address}")
    case UnreachableMember(member) =>
                          println(s"*** Member Unreachable: $self ${member.address}")
    case MemberRemoved(member, previousStatus) =>
                          println(s"*** Member is Removed: $self ${member.address} after $previousStatus")
    case MemberExited(member) =>
                          println(s"*** Member is Exited: $self ${member.address}")
    case LeaderChanged(leader) =>
                          println(s"*** Leader is Changed: $self $leader")
    case evt: MemberEvent =>
                          println(s"*** Memver event $self ${evt.member.status} ${evt.member.address}")

  }


  val matchEnvelopeContents : (SuccessEnvelope[WsMessageDown], WsMessageDown) => Unit =
    (envelope, message) => {
      message match {
        case message : WsTextDown   => {
             publishToChatRoom(CommonUtils.
                                 getChatId(envelope.source,
                                           envelope.targets), envelope)
             envelope.targets.foreach(target =>
                                        publishToMailbox(target,envelope))
        }
        case message : WsBinaryDown => {
             publishToChatRoom(CommonUtils.
                                  getChatId(envelope.source,
                                            envelope.targets), envelope)
             envelope.targets.foreach(target =>
                                        publishToMailbox(target, envelope))
        }
        case message : UserOnline => {
             CommonUtils.consoleLog("DEBUG", "User Online")
             streamReference = message.actorRef
        }

        case _ =>  CommonUtils.consoleLog("DEBUG", "User Online")

      }
    }





}
