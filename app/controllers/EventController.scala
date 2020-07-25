package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import javax.inject.Inject
import models.{AccredsModel, EventsModel}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.ExecutionContext

/**
 * @author Louis Vialar
 */
class EventController @Inject()(cc: ControllerComponents, model: EventsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getEvent: Action[AnyContent] = Action.async { implicit rq =>
    model.getCurrentEvent.map(ev => Ok(Json.toJson(ev))).recover {
      case _ => NotFound
    }
  }.requiresAuthentication

}
