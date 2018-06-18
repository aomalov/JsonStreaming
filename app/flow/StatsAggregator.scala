package flow

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import model.InputData
import play.api.Logger
import play.libs.F.Tuple

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

case class StatsAggregator(statsToken: InputData=>String) extends GraphStage[FlowShape[Try[InputData],Tuple[String,Long]]] {

  val in = Inlet[Try[InputData]]("Aggregator.in")
  val out = Outlet[Tuple[String,Long]]("Aggregator.out")

  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      var stats: mutable.Map[String,Long]  = mutable.HashMap.empty[String,Long] withDefaultValue 0

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          grab(in) match {
            case Success(input) =>
              Logger.debug(s"GOT object $input")
              val key:String=statsToken(input)
              stats.update(key,stats(key)+1)
              push(out,Tuple(key,stats(key)))
            case Failure(exception) =>
              Logger.warn(s"Bad input [${exception.getMessage}]")
              pull(in)
          }
        }
      })
      //Passing request upstream transparently
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }

}
