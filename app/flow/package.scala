import model.Jsonable
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

package object flow {
  implicit object JsonOpsForMap extends Jsonable[Map[String,Long]] {
    override def convertToJson(obj: Map[String, Long]): JsValue = Json.toJson(obj)
  }

  implicit class JsonOpsHelper[T:Jsonable](obj:T) {
    def toJson: JsValue = Jsonable[T].convertToJson(obj)
  }

}
