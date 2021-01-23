package controllers

import java.time.Clock

import data.{Accred, AccredStatus, AdminUser, StaffUser}
import javax.inject.Inject
import models.{AccountsModel, AccredsModel}
import play.api.Configuration
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.AuthenticationPostfix._

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Louis Vialar
 */
class AccredsController @Inject()(cc: ControllerComponents, model: AccredsModel, accounts: AccountsModel)(implicit ec: ExecutionContext, conf: Configuration, clock: Clock) extends AbstractController(cc) {

  def getAccreds: Action[AnyContent] = Action.async { implicit rq =>
    model.getAccreds(rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAuthentication

  def getAccred(id: Int): Action[AnyContent] = Action.async { implicit rq =>
    model.getAccred(id).map {
      case Some(res) => Ok(Json.toJson(res))
      case None => NotFound
    }
  }.requiresAuthentication

  case class SetState(targetState: AccredStatus.Value, remarks: Option[String], firstName: Option[String], lastName: Option[String], number: Option[String])

  implicit val SetStateFormat: Format[SetState] = Json.format[SetState]

  def setState(id: Int): Action[SetState] = Action.async(parse.json[SetState]) { implicit rq =>
    val target = rq.body.targetState

    if (target == AccredStatus.Granted) Future(BadRequest)
    else {
      val (staff, admin) = rq.user match {
        case AdminUser(userId, _, _) => (None, Some(userId))
        case StaffUser(staffUserId) => (Some(staffUserId), None)
      }

      // First, get the accred
      model.getFullAccred(id).flatMap {
        case Some((accred, tpe)) =>
          val nameRequired = accred.requireRealNameOnDelivery && target == AccredStatus.Delivered && (accred.firstname.isEmpty || accred.lastname.isEmpty)

          if (nameRequired && (rq.body.firstName.isEmpty || rq.body.lastName.isEmpty))
            Future(BadRequest)
          else if (tpe.physicalAccredType.get.physicalAccredTypeNumbered && rq.body.number.isEmpty)
            Future(BadRequest)
          else {
            model.updateAccredStatus(id, target, staff, admin, rq.body.remarks.getOrElse(""),
              rq.body.firstName.filter(_ => nameRequired),
              rq.body.lastName.filter(_ => nameRequired),
              rq.body.number.filter(_ => tpe.physicalAccredType.get.physicalAccredTypeNumbered),
            ).map(res => Ok(Json.toJson(res)))
          }
        case None => Future(NotFound)
      }
    }
  }.requiresAuthentication

  def createAccred: Action[Accred] = Action.async(parse.json[Accred]) { implicit rq =>
    val userId = rq.user.asInstanceOf[AdminUser].userId
    val toCreate = rq.body.copy(None, authoredBy = userId, status = AccredStatus.Granted)

    model.createAccred(toCreate, rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  case class DelegatedAccredCreation(accred: Accred, delegationKey: String)

  implicit val DelegatedAccredCreationFormat: Format[DelegatedAccredCreation] = Json.format[DelegatedAccredCreation]

  def createAccredDelegated: Action[DelegatedAccredCreation] = Action.async(parse.json[DelegatedAccredCreation]) { implicit rq =>
    val key = rq.body.delegationKey.trim.toUpperCase.replaceAll("[^A-F0-9]", "")

    if (key.length < 5 || key.length > 20) {
      Future(Forbidden)
    } else {
      accounts.getOneTimeKey(key).flatMap {
        case Some(userId) =>
          val toCreate = rq.body.accred.copy(None, authoredBy = userId, status = AccredStatus.Granted, details = Some(rq.body.accred.details.getOrElse("") + " - Créé par délégation au compte staff " + rq.user.asInstanceOf[StaffUser].staffUserId))
          model.createAccred(toCreate, rq.eventId).map(res => Ok(Json.toJson(res)))

        case None => Future(Forbidden)
      }
    }
  }.requiresAuthentication

  def createAccreds: Action[List[Accred]] = Action.async(parse.json[List[Accred]]) { implicit rq =>
    val userId = rq.user.asInstanceOf[AdminUser].userId

    model.createAccreds(userId, rq.body, rq.eventId).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

  def updateAccred(id: Int): Action[Accred] = Action.async(parse.json[Accred]) { implicit rq =>
    model.updateAccred(id, rq.body).map(res => Ok(Json.toJson(res > 0)))
  }.requiresAdmin

  def deleteAccred(id: Int) = Action.async { implicit rq =>
    model.deleteAccred(id).map(res => Ok(Json.toJson(res > 0)))
  }.requiresAdmin

  def getLogs(id: Int) = Action.async { implicit rq =>
    model.getLogs(id).map(res => Ok(Json.toJson(res)))
  }.requiresAdmin

}
