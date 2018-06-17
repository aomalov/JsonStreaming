import model.InputData
import play.api.libs.json.{Json, OFormat}

package object services {
  implicit val inputDataFormat: OFormat[InputData] = Json.format[InputData]
}
