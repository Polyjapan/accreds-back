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
    model.getAccredTypes(rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getPhysicalAccredTypes: Action[AnyContent] = Action.async { implicit rq =>
    model.getPhysicalAccredTypes(rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getFullAccredTypes: Action[AnyContent] = Action.async { implicit rq =>
    model.getFullAccredTypes(rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def createAccredType: Action[AccredType] = Action.async(parse.json[AccredType]) { implicit rq =>
    model.createAccredType(rq.body, rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  def createPhysicalAccredType: Action[PhysicalAccredType] = Action.async(parse.json[PhysicalAccredType]) { implicit rq =>
    model.createPhysicalAccredType(rq.body, rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresGroup("securite")

  def createUpdateAccredTypeMapping: Action[List[(Int, Int)]] = Action.async(parse.json[List[(Int, Int)]]) { implicit rq =>
    model.createUpdateAccredTypeMapping(rq.body).map(_ => Ok)
  }.requiresGroup("securite")

  // Mapping to create as well

}
