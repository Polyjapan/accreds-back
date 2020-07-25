package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import javax.inject.Inject
import models.AccredsModel
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.ExecutionContext

/**
 * @author Louis Vialar
 */
class PeopleController @Inject()(cc: ControllerComponents, auth: AuthApi, model: AccredsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getAdmins: Action[AnyContent] = Action.async { implicit rq =>
    model.getAuthors(rq.eventId).flatMap(ids => auth.getUserProfiles(ids).map {
      case Left(map) => Ok(Json.toJson(map.values.map(v => v.copy(address = None))))
      case _ => InternalServerError
    })
  }.requiresAuthentication

}
