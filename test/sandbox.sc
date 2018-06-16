import play.api.libs.json._

import scala.util.Try

val cnt=1
val t=Try(Json.parse(s"""{\"total\":$cnt}"""))
println(t)
val t1:Try[JsValue] = Try(Json.parse("{sdfsdf"))
val m1=Map("bar" -> 1)
val m2=Map("bar" -> 2, "baz" -> 3)

m2.foldLeft(m1) {
  (m,entry)=>m+(entry._1->(m.getOrElse(entry._1,0)+entry._2))
}