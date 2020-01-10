package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import data.VipDesk
import javax.inject.Inject
import models.{AccredTypesModel, VipDesksModel}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.ExecutionContext

/**
 * @author Louis Vialar
 */
class VipDesksController @Inject()(cc: ControllerComponents, auth: AuthApi, model: VipDesksModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getDesks: Action[AnyContent] = Action.async { implicit rq =>
    model.getVipDesks.map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def createDesk: Action[VipDesk] = Action.async(parse.json[VipDesk]) { implicit rq =>
    model.createVipDesk(rq.body).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

}