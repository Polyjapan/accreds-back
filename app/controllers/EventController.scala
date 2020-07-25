package controllers

import java.time.Clock

import data.{AdminUser, UserSession}
import javax.inject.Inject
import models.EventsModel
import pdi.jwt.JwtSession
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
    (
      if (rq.eventId.nonEmpty) model.getEvent(rq.eventId.get)
      else model.getCurrentEvent
      ).map(ev => Ok(Json.toJson(ev))).recover {
      case _ => NotFound
    }
  }.requiresAuthentication

  def getEvents: Action[AnyContent] = Action.async { implicit rq =>
    model.getEvents.map(ev => Ok(Json.toJson(ev))).recover {
      case _ => NotFound
    }
  }.requiresAdmin

  def switchEvent(id: Int) = Action { implicit rq =>
    val session = rq.user.asInstanceOf[AdminUser]
    val newSession: JwtSession = JwtSession() + ("user", session.asInstanceOf[UserSession]) + ("event", id)

    Ok(Json.toJson(Json.obj("session" -> newSession.serialize)))
  }.requiresAdmin

}
