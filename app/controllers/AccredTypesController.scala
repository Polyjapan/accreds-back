package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import data.{AccredType, PhysicalAccredType}
import javax.inject.Inject
import models.AccredTypesModel
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.ExecutionContext

/**
 * @author Louis Vialar
 */
class AccredTypesController @Inject()(cc: ControllerComponents, auth: AuthApi, model: AccredTypesModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getAccredTypes: Action[AnyContent] = Action.async { implicit rq =>
    model.getAccredTypes().map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getPhysicalAccredTypes: Action[AnyContent] = Action.async { implicit rq =>
    model.getPhysicalAccredTypes().map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getFullAccredTypes: Action[AnyContent] = Action.async { implicit rq =>
    model.getFullAccredTypes().map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def createAccredType: Action[AccredType] = Action.async(parse.json[AccredType]) { implicit rq =>
    model.createAccredType(rq.body).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  def createPhysicalAccredType: Action[PhysicalAccredType] = Action.async(parse.json[PhysicalAccredType]) { implicit rq =>
    model.createPhysicalAccredType(rq.body).map(res => Ok(Json.toJson(res)))
  }.requiresGroup("securite")

  // Mapping to create as well

}
