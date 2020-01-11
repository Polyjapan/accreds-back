package controllers

import java.time.Clock

import ch.japanimpact.auth.api.AuthApi
import data.{AdminUser, StaffUser, UserSession}
import javax.inject.Inject
import models.AccountsModel
import pdi.jwt.JwtSession
import play.api.Configuration
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Louis Vialar
 */
class LoginController @Inject()(cc: ControllerComponents, auth: AuthApi, accounts: AccountsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def login(ticket: String): Action[AnyContent] = Action.async { implicit rq =>
    if (auth.isValidTicket(ticket)) {
      auth.getAppTicket(ticket).map {
        case Left(ticketResponse) if ticketResponse.ticketType.isValidLogin =>
          if (!ticketResponse.groups("comite-ji")) Forbidden
          else {
            val session: JwtSession = JwtSession() + ("user", AdminUser(ticketResponse).asInstanceOf[UserSession])

            Ok(Json.toJson(Json.obj("session" -> session.serialize)))
          }
        case Right(_) => BadRequest
      }
    } else Future(BadRequest)
  }

  case class GrantStaffRequest(vipDeskId: Int, name: String)

  implicit val requestParser: Format[GrantStaffRequest] = Json.format[GrantStaffRequest]

  def grantStaff: Action[GrantStaffRequest] = Action.async(parse.json[GrantStaffRequest]) { implicit rq =>
    accounts.createStaffAccount(rq.body.vipDeskId, rq.body.name, rq.user.asInstanceOf[AdminUser].userId)
      .map {
        case Some(accountId) =>
          val session: JwtSession = JwtSession() + ("user", StaffUser(accountId).asInstanceOf[UserSession])

          Ok(Json.toJson(Json.obj("session" -> session.serialize)))
        case None => InternalServerError
      }
  }.requiresAdmin

  def createDelegationKey = Action.async { implicit rq =>
    accounts.createOneTimeKey(rq.user.asInstanceOf[AdminUser].userId)
      .map {
        case Some(key) => Ok(key)
        case None => InternalServerError
      }
  }.requiresAdmin

}
