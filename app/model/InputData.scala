package model

import play.api.libs.json.JsValue

case class InputData(event_type:String,data:String,timestamp:Long)

trait Jsonable[T]  {
  def convertToJson(obj: T):JsValue
}

object Jsonable {
  def apply[T:Jsonable]: Jsonable[T] = implicitly
}