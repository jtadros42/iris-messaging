package com.company.iris.messages

import com.company.iris.utils.CommonUtils
import org.scalatest.FunSuite
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

class MessageSpec extends FunSuite {
  val requestWellFormed:     String     = "{\"user_id\":\"jtadros\",\"chat_id\":\"someid\",\"content\":\"YOYOYOY\"}"
  val requestMissingContent: String     = "{\"user_id\":\"jtadros\",\"chat_id\":\"someid\"}"
  val illFormedJson:         String     = "{\"user_id\":\"jtadros\"\"chat_id\":\"someid\"}"
  val jsonArray:             String     = "{\"targets\": [\"jtadros\", \"johnluke\"] }"


  test("Test Parsing json Array"){
    val targets     = Json.parse(jsonArray)
    val targetField = (targets \ "targets").as[Array[String]]
    print()
  }


}
