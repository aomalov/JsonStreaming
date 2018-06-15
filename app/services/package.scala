import model.InputData
import play.api.libs.json.{Json, OFormat}

package object services {
  implicit val wordsSummaryFormat: OFormat[WordSummary] = Json.format[WordSummary]
  implicit val eventSummaryFormat: OFormat[EventSummary] = Json.format[EventSummary]
  implicit val inputDataFormat: OFormat[InputData] = Json.format[InputData]
}
