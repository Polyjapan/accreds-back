package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import data.{Accred, AccredStatus, AdminUser}
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
class AccredsController @Inject()(cc: ControllerComponents, auth: AuthApi, model: AccredsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getAccreds: Action[AnyContent] = Action.async { implicit rq =>
    model.getAccreds().map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getAccred(id: Int): Action[AnyContent] = Action.async { implicit rq =>
    model.getAccred(id).map {
      case Some(res) => Ok(Json.toJson(res))
      case None => NotFound
    }
  }.requiresAuthentication

  def createAccred: Action[Accred] = Action.async(parse.json[Accred]) { implicit rq =>
    val userId = rq.user.asInstanceOf[AdminUser].userId
    val toCreate = rq.body.copy(None, authoredBy = userId, status = AccredStatus.Granted)

    model.createAccred(toCreate).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  def createAccreds: Action[List[Accred]] = Action.async(parse.json[List[Accred]]) { implicit rq =>
    val userId = rq.user.asInstanceOf[AdminUser].userId

    model.createAccreds(userId, rq.body).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  def updateAccred(id: Int): Action[Accred] = Action.async(parse.json[Accred]) { implicit rq =>
    model.updateAccred(id, rq.body).map(res => Ok(Json.toJson(res > 0)))
  }.requiresAdmin

}
