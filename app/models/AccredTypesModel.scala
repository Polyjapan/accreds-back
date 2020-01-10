package models

import anorm.SqlParser._
import anorm._
import data._
import data.returnTypes.FullAccredType
import javax.inject.{Inject, Singleton}
import utils.SqlUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccredTypesModel @Inject()(dbApi: play.api.db.DBApi, events: EventsModel)(implicit ec: ExecutionContext) {
  private val db = dbApi database "default"

  def getAccredTypes(): Future[List[AccredType]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM accred_types").as(AccredTypeRowParser.*)
  })

  def getPhysicalAccredTypes(): Future[List[PhysicalAccredType]] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM physical_accred_types").as(PhysicalAccredTypeRowParser.*)
  })

  def createAccredType(tpe: AccredType): Future[Int] = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("accred_types", tpe)
  })

  def createPhysicalAccredType(tpe: PhysicalAccredType): Future[Int] = Future(db.withConnection { implicit conn =>
    SqlUtils.insertOne("physical_accred_types", tpe)
  })

  def getFullAccredTypes(): Future[List[FullAccredType]] = Future(db.withConnection { implicit conn =>
    val eventId = events.getCurrentEventIdSync()

    SQL("SELECT accred_types.*, physical_accred_types.* FROM accred_types LEFT JOIN accred_type_mappings atm on accred_types.accred_type_id = atm.accred_type_id AND atm.event_id = {eventId} LEFT JOIN physical_accred_types on atm.physical_accred_type_id = physical_accred_types.physical_accred_type_id")
      .on("eventId" -> eventId)
      .as(FullAccredTypeRowParser.*)
  })

}
