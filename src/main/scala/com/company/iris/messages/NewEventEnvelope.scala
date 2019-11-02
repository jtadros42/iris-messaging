package com.company.iris.messages

trait Event

sealed abstract class NewEventEnvelope(val eventTime  : String,
                                    val ingestionTime : String,
                                    val source        : String,
                                    val targets       : Array[String],
                                    val event         : Event) extends Product with Serializable {
  def isFailure: Boolean
  def isSuccess: Boolean
  def foreach(f: Event => Event): Unit
  def get: Event
  def flatMap(f: Event => NewEventEnvelope): NewEventEnvelope
  def map[U](f: Event => Event): NewEventEnvelope
  def filter(p: Event => Boolean): NewEventEnvelope
}
