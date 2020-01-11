package models

import anorm.Macro.ColumnNaming
import anorm.SqlParser._
import anorm._
import data._
import javax.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventsModel @Inject()(dbApi: play.api.db.DBApi, cache: SyncCacheApi)(implicit ec: ExecutionContext) {
  private val db = dbApi database "default"

  def getCurrentEventIdSync: Int = db.withConnection { implicit conn =>
    cache.getOrElseUpdate("current_event_id", 30.minutes) {
      SQL("SELECT event_id FROM events ORDER BY event_id DESC LIMIT 1").as(int("event_id").single)
    }
  }

  def getCurrentEventIdAsync: Future[Int] = Future(getCurrentEventIdSync)

  def getCurrentEvent: Future[Event] = Future(db.withConnection { implicit conn =>
    SQL("SELECT * FROM events ORDER BY event_id DESC LIMIT 1").as(EventRowParser.single)
  })
}
