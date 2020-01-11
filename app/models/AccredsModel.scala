package models

import anorm._
import data._
import javax.inject.{Inject, Singleton}
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccredsModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {
  private val db = dbApi database "default"
  private def eventId = events.getCurrentEventIdSync

  def getAccreds: Future[List[Accred]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accreds WHERE deleted = 0 AND event_id = {eventId}")
      .on("eventId" -> eventId)
      .as(AccredRowParser.*)
  })

  def getAccred(id: Int): Future[Option[Accred]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accreds WHERE deleted = 0 AND accred_id = {id}")
      .on("id" -> id)
      .as(AccredRowParser.singleOpt)
  })

  def createAccred(accred: Accred) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("accreds", accred.copy(eventId = eventId))
  })

  def createAccreds(author: Int, accreds: List[Accred]) = Future(db.withConnection { implicit conn =>
    SqlUtils.insertMultiple("accreds", accreds.map(accred => accred.copy(None, authoredBy = author, status = AccredStatus.Granted, eventId = eventId)))
  })

  def updateAccred(id: Int, accred: Accred): Future[Int] = Future(db.withConnection { implicit conn =>
    val params: Seq[NamedParameter] = AccredParameterList(accred)

    SQL("UPDATE accreds SET lastname = {lastname}, firstname = {firstname}, body_name = {bodyName}, stage_name = {stageName}, must_contact_admin = {mustContactAdmin}, details = {details}, accred_type_id = {accredTypeId}, prefered_vip_desk = {preferedVipDesk} WHERE accred_id = {accredId}")
      .on(params:_*)
      .executeUpdate()
  })

  def deleteAccred(id: Int): Future[Int] = Future(db.withConnection { implicit conn =>
    SQL("UPDATE accreds SET deleted = 1 WHERE accred_id = {accredId}")
      .on("accredId" -> id)
      .executeUpdate()
  })

}
