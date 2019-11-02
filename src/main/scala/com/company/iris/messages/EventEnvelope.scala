package com.company.iris.messages

import scala.util.control.NonFatal






sealed abstract class EventEnvelope[+T](val eventTime     : String,
                                        val ingestionTime : String,
                                        val source        : String,
                                        val targets       : Array[String]) extends Product with Serializable {
  def isFailure: Boolean
  def isSuccess: Boolean
  def foreach[U](f: T => U): Unit
  def get: T
  def flatMap[U](f: T => EventEnvelope[U]): EventEnvelope[U]
  def map[U](f: T => U): EventEnvelope[U]
  def collect[U](pf: PartialFunction[T, U]): EventEnvelope[U]
  def filter(p: T => Boolean): EventEnvelope[T]
  def flatten[U](implicit ev: T <:< EventEnvelope[U]): EventEnvelope[U]
  def fold[U](fa: Throwable => U, fb: T => U): U
}


object EventEnvelope{

  def apply[T](eventTime: String, ingestionTime: String, source: String, targets: Array[String], r : => T): EventEnvelope[T] =
      try(SuccessEnvelope(eventTime, ingestionTime, source, targets, r))
      catch {
        case NonFatal(e) => FailureEnvelope(eventTime, ingestionTime, source, targets, e)
      }
}


final case class FailureEnvelope[+T](override val eventTime :     String,
                                     override val ingestionTime : String,
                                     override val source        : String,
                                     override val targets       : Array[String],
                                                  exception:     Throwable) extends EventEnvelope[T](
                                                                                      eventTime     = eventTime,
                                                                                      ingestionTime = ingestionTime,
                                                                                      source        = source,
                                                                                      targets       = targets){
  override def isFailure: Boolean = true
  override def isSuccess: Boolean = false

  override def get: T = throw exception
  override def flatMap[U](f: T => EventEnvelope[U]): EventEnvelope[U]            = this.asInstanceOf[EventEnvelope[U]]
  override def flatten[U](implicit ev: T <:< EventEnvelope[U]): EventEnvelope[U] = this.asInstanceOf[EventEnvelope[U]]
  override def foreach[U](f: T => U): Unit                                       = ()
  override def map[U](f: T => U)                      : EventEnvelope[U]         = this.asInstanceOf[EventEnvelope[U]]
  override def collect[U](pf: PartialFunction[T, U])  : EventEnvelope[U]         = this.asInstanceOf[EventEnvelope[U]]
  override def filter(p: T => Boolean)                : EventEnvelope[T]         = this
  override def fold[U](fa: Throwable => U, fb: T => U): U                        = fa(exception)

}




final case class SuccessEnvelope[+T](override val eventTime     : String,
                                     override val ingestionTime : String,
                                     override val source        : String,
                                     override val targets       : Array[String],
                                         value: T) extends EventEnvelope[T](eventTime     = eventTime,
                                                                            ingestionTime = ingestionTime,
                                                                            source        = source,
                                                                            targets       = targets){

  override def isFailure: Boolean      = false
  override def isSuccess: Boolean      = true
  override def get: T                  = value

  override def flatMap[U](f: T => EventEnvelope[U]): EventEnvelope[U] =
    try f(value)
    catch {
      case NonFatal(e) => FailureEnvelope[U](eventTime, ingestionTime, source, targets, e)
    }

  override def flatten[U](implicit ev: T <:< EventEnvelope[U]): EventEnvelope[U] = value
  override def foreach[U](f: T => U): Unit                                       = f(value)
  override def map[U](f: T => U): EventEnvelope[U]                               =
    try{
      SuccessEnvelope(eventTime, ingestionTime, source, targets, f(value))
    }catch {
      case NonFatal(e) => FailureEnvelope(eventTime, ingestionTime, source, targets, e)
    }


  override def collect[U](pf: PartialFunction[T, U]): EventEnvelope[U] =
    try {
      if (pf isDefinedAt value)
        SuccessEnvelope(eventTime, ingestionTime, source, targets, pf(value))
      else
        FailureEnvelope(eventTime, ingestionTime, source, targets, new NoSuchElementException("Predicate does not hold for " + value))
    } catch {
      case NonFatal(e) => FailureEnvelope(eventTime, ingestionTime, source, targets, e)
    }


  override def filter(p: T => Boolean): EventEnvelope[T] =
    try {
      if (p(value))
        this
      else
        FailureEnvelope(eventTime, ingestionTime, source, targets, new NoSuchElementException("Predicate does not hold for " + value))
    } catch {
      case NonFatal(e) => FailureEnvelope(eventTime, ingestionTime, source, targets, e)
    }


  override def fold[U](fa: Throwable => U, fb: T => U): U =
    try {
      fb(value)
    } catch{
      case NonFatal(e) => fa(e)
    }
}