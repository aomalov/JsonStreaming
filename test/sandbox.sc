import play.api.libs.json._

import scala.util.Try

val cnt=1
val t=Try(Json.parse(s"""{\"total\":$cnt}"""))
println(t)
val t1:Try[JsValue] = Try(Json.parse("{sdfsdf"))
