package com.company.iris.utils



import akka.util.ByteString
import com.company.iris.messages.{EventEnvelope, FailureEnvelope, SuccessEnvelope, WsBinaryUp, WsMessageDown, WsMessageUp, WsTextUp}
import org.joda.time.{DateTime, LocalDateTime}
import play.api.libs.json.{JsString, JsValue, Json}
import scala.annotation.tailrec
import scala.collection.SortedSet
import scala.util.{Failure, Success, Try}
import java.security.MessageDigest
import java.math.BigInteger



object CommonUtils {

  private val messageDigestID : String = "MD5"
  private val signBit : Int            = 1
  private val radix   : Int            = 16
  private val dateFormat : String      = "yyyy-MM-dd HH:mm:ss"


  def getChatId(source: String, targets: Array[String]): String = {
      val chatMembers : SortedSet[String] = createSortedSet(targets.toList) + source
      val concatenatedString : String     = chatMembers.fold("")(_ + _)
      getHashString(concatenatedString)
  }


  def createSortedSet(targets: List[String]): SortedSet[String] = {
      @tailrec
      def iterate(sortedSet: SortedSet[String], s : List[String]) : SortedSet[String] = {
        s match {
          case head::rest => iterate((sortedSet+head), rest)
          case Nil => sortedSet
        }
      }
      iterate(SortedSet(), targets)
  }

  def getHashString(str: String): String = {
    val digest       = MessageDigest.getInstance(messageDigestID).digest(str.getBytes)
    val bigInt       = new BigInteger(signBit, digest)
    val hashedString = bigInt.toString(radix)
    hashedString
  }

  def getJsonString(json: JsValue, field: String, default: String = ""): String = {
    val ret = (json \ field).getOrElse(JsString(default)).as[String]
    ret
  }

  def getJsonStringArray(json: JsValue, field: String): Array[String] = {
    (json \ field).as[Array[String]]
  }


  def wrapEnvelope[T](event: T): EventEnvelope[T] = {
      val time   : String = LocalDateTime.now().toString()
      val source : String = "";
      val target : Array[String] = Array.empty[String]
      EventEnvelope(time, time, source, target, event)
  }


  def consoleLog(logType: String, msg: String) = {
    val timeStr = new DateTime().toString(dateFormat)
    println(s"[$logType] $timeStr: $msg")
  }


  def toJson(envelope : EventEnvelope[WsMessageDown]) : String = {
        "{}"
  }


  def parseTextMessage(jsonStr : String): EventEnvelope[WsMessageUp] = {
      val json                        = Try(Json.parse(jsonStr))
      val eventTime                   = ""
      val ingestionTime               = LocalDateTime.now().toString()
      val source:  Try[String]        = json.map(js => CommonUtils.getJsonString(js,      "source"))
      val target:  Try[Array[String]] = json.map(js => CommonUtils.getJsonStringArray(js, "targets"))
      val content: Try[String]        = json.map(js => CommonUtils.getJsonString(js,      "content"))
      val result:  Try[EventEnvelope[WsMessageUp]]  = source.flatMap(src =>
        target.flatMap(tgt =>
          content.flatMap(cnt => Try(SuccessEnvelope[WsMessageUp](eventTime, ingestionTime, src, tgt, WsTextUp(cnt)))
          )))
      result match {
        case Success(value)     => value
        case Failure(exception) => FailureEnvelope[WsMessageUp]("", ingestionTime, "", Array.empty[String], exception)
      }
  }




  def parseBinaryMessage(str: ByteString): EventEnvelope[WsMessageUp] = {
    val json                        = Try(Json.parse(str.utf8String))
    val eventTime                   = ""
    val ingestionTime               = LocalDateTime.now().toString()
    val source:  Try[String]        = json.map(js => CommonUtils.getJsonString(js,      "source"))
    val target:  Try[Array[String]] = json.map(js => CommonUtils.getJsonStringArray(js, "targets"))
    val content: Try[String]        = json.map(js => CommonUtils.getJsonString(js,      "content"))
    val result:  Try[EventEnvelope[WsMessageUp]]  = source.flatMap(src =>
      target.flatMap(tgt =>
        content.flatMap(cnt => Try(SuccessEnvelope[WsMessageUp](eventTime, ingestionTime, src, tgt, WsBinaryUp(cnt)))
        )))
    result match {
      case Success(value)     => value
      case Failure(exception) => FailureEnvelope[WsMessageUp]("", ingestionTime, "", Array.empty[String], exception)
    }
  }



  /*

  def parseBinaryMessage(str: ByteString): WsMessageUp = {
      val json                      = Try(Json.parse(str.utf8String))
      val userId:  Try[String]      = json.map(js => CommonUtils.getJsonString(js, "user_id"))
      val chatId:  Try[String]      = json.map(js => CommonUtils.getJsonString(js, "chat_id"))
      val content: Try[String]      = json.map(js => CommonUtils.getJsonString(js, "content"))
      val result : Try[WsMessageUp] = userId.flatMap(uid => chatId.flatMap(cid => content.flatMap(cnt => Try(WsBinaryUp(uid, cid, cnt)))))
      result match {
        case Success(value)     => value
        case Failure(exception) => WsFailedUp("", "", exception.getMessage)
      }
  }

   */

}
