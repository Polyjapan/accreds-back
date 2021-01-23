package controllers

import data.VipDesk
import models.VipDesksModel
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
class VipDesksController @Inject()(cc: ControllerComponents, model: VipDesksModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getDesks: Action[AnyContent] = Action.async { implicit rq =>
    model.getVipDesks(rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def createDesk: Action[VipDesk] = Action.async(parse.json[VipDesk]) { implicit rq =>
    model.createVipDesk(rq.body, rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

}
