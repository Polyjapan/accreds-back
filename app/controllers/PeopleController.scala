package controllers

import ch.japanimpact.auth.api.{UserProfile, UsersApi}
import models.AccredsModel
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.ExecutionContext

/**
 * @author Louis Vialar
 */
class PeopleController @Inject()(cc: ControllerComponents, auth: UsersApi, model: AccredsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getAdmins: Action[AnyContent] = Action.async { implicit rq =>
    model.getAuthors(rq.eventId).flatMap(ids => auth.getUsersWithIds(ids).map {
      case Right(lst) =>
        Ok {
          Json.toJson {
            ids.map(id => {
              val userData = lst(id)
              UserProfile(id, userData.email, userData.details, None)
            })
          }
        }
      case _ => InternalServerError
    })
  }.requiresAuthentication

}
