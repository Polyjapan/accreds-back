import java.sql.PreparedStatement

import anorm.Macro.ColumnNaming
import anorm.{Column, Macro, RowParser, ToParameterList, ToStatement, ~}
import data.returnTypes.{FullAccredLog, FullAccredType, FullStaffAccount}
import org.joda.time.DateTime
import play.api.libs.json._
import anorm.JodaParameterMetaData._ // VERY IMPORTANT IMPLICIT!

package object data {
  case class VipDesk(vipDeskId: Option[Int], vipDeskName: String)

  case class PhysicalAccredType(physicalAccredTypeId: Option[Int], physicalAccredTypeName: String, physicalAccredTypeNumbered: Boolean, eventId: Option[Int])

  case class AccredType(accredTypeId: Option[Int], accredTypeName: String, requiresSignature: Boolean, isTemporary: Boolean)

  object AccredStatus extends Enumeration {
    type AccredStatus = Value
    val Granted, Delivered, Recovered = Value
  }

  implicit def StatusColumn: Column[AccredStatus.Value] =
    Column.columnToString.map {
      case "GRANTED" => AccredStatus.Granted
      case "DELIVERED" => AccredStatus.Delivered
      case "RECOVERED" => AccredStatus.Recovered
    }

  implicit val StatusStatement: ToStatement[AccredStatus.Value] = (s: PreparedStatement, index: Int, v: AccredStatus.Value) =>
    s.setString(index, v match {
      case AccredStatus.Granted => "GRANTED"
      case AccredStatus.Delivered => "DELIVERED"
      case AccredStatus.Recovered => "RECOVERED"
    })

  implicit val StatusFormat: Format[AccredStatus.Value] = Json.formatEnum(AccredStatus)

  case class AccredTypeMapping(eventId: Int, accredTypeId: Int, physicalAccredTypeId: Int)

  case class Accred(accredId: Option[Int], eventId: Int, firstname: String, lastname: String, bodyName: String, stageName: String, details: Option[String], authoredBy: Int, accredTypeId: Int, status: AccredStatus.Value, preferedVipDesk: Int, mustContactAdmin: Boolean, requireRealNameOnDelivery: Boolean)

  case class StaffAccount(staffAccountId: Option[Int], eventId: Int, vipDeskId: Int, name: String, authoredBy: Int, authoredAt: Option[DateTime])

  case class AccredLog(accredLogId: Option[Int], accredLogTime: Option[DateTime], accredId: Int, authoredByAdmin: Option[Int], authoredByStaff: Option[Int], sourceState: AccredStatus.Value, targetState: AccredStatus.Value, remarks: Option[String], accredNumber: Option[String])

  object returnTypes {
    case class FullAccredType(accredType: AccredType, physicalAccredType: Option[PhysicalAccredType])

    case class FullAccred(accred: Accred, accredType: FullAccredType, preferedVipDesk: VipDesk)

    case class FullStaffAccount(account: StaffAccount, vipDesk: VipDesk)

    case class FullAccredLog(log: AccredLog, staff: Option[FullStaffAccount])
  }

  implicit val datetimeRead: Reads[DateTime] = JodaReads.DefaultJodaDateTimeReads
  implicit val datetimeWrite: Writes[DateTime] = JodaWrites.JodaDateTimeWrites

  implicit val VipDeskFormat: OFormat[VipDesk] = Json.format[VipDesk]
  implicit val VipDeskParameterList: ToParameterList[VipDesk] = Macro.toParameters[VipDesk]()
  implicit val VipDeskRowParser: RowParser[VipDesk] = Macro.namedParser[VipDesk]((p: String) => "vip_desks." + ColumnNaming.SnakeCase(p))

  implicit val PhysicalAccredTypeFormat: OFormat[PhysicalAccredType] = Json.format[PhysicalAccredType]
  implicit val PhysicalAccredTypeParameterList: ToParameterList[PhysicalAccredType] = Macro.toParameters[PhysicalAccredType]()
  implicit val PhysicalAccredTypeRowParser: RowParser[PhysicalAccredType] = Macro.namedParser[PhysicalAccredType]((p: String) => "physical_accred_types." + ColumnNaming.SnakeCase(p))

  implicit val AccredTypeFormat: OFormat[AccredType] = Json.format[AccredType]
  implicit val AccredTypeParameterList: ToParameterList[AccredType] = Macro.toParameters[AccredType]()
  implicit val AccredTypeRowParser: RowParser[AccredType] = Macro.namedParser[AccredType]((p: String) => "accred_types." + ColumnNaming.SnakeCase(p))

  implicit val AccredTypeMappingFormat: OFormat[AccredTypeMapping] = Json.format[AccredTypeMapping]
  implicit val AccredTypeMappingParameterList: ToParameterList[AccredTypeMapping] = Macro.toParameters[AccredTypeMapping]()
  implicit val AccredTypeMappingRowParser: RowParser[AccredTypeMapping] = Macro.namedParser[AccredTypeMapping]((p: String) => "accred_type_mappings." + ColumnNaming.SnakeCase(p))

  implicit val AccredFormat: OFormat[Accred] = Json.format[Accred]
  implicit val AccredParameterList: ToParameterList[Accred] = Macro.toParameters[Accred]()
  implicit val AccredRowParser: RowParser[Accred] = Macro.namedParser[Accred](ColumnNaming.SnakeCase)

  implicit val StaffAccountFormat: OFormat[StaffAccount] = Json.format[StaffAccount]
  implicit val StaffAccountParameterList: ToParameterList[StaffAccount] = Macro.toParameters[StaffAccount]()
  implicit val StaffAccountRowParser: RowParser[StaffAccount] = Macro.namedParser[StaffAccount]((p: String) => "staff_accounts." + ColumnNaming.SnakeCase(p))

  implicit val AccredLogFormat: OFormat[AccredLog] = Json.format[AccredLog]
  implicit val AccredLogParameterList: ToParameterList[AccredLog] = Macro.toParameters[AccredLog]()
  implicit val AccredLogRowParser: RowParser[AccredLog] = Macro.namedParser[AccredLog]((p: String) => "accred_logs." + ColumnNaming.SnakeCase(p))

  implicit val FullAccredTypeRowParser: RowParser[FullAccredType] = (AccredTypeRowParser ~ PhysicalAccredTypeRowParser.?) map { case tpe ~ ptpe => FullAccredType(tpe, ptpe) }
  implicit val FullAccredTypeFormat: OFormat[FullAccredType] = Json.format[FullAccredType]

  implicit val FullStaffAccountFormat: OFormat[FullStaffAccount] = Json.format[FullStaffAccount]
  implicit val FullAccredLogFormat: OFormat[FullAccredLog] = Json.format[FullAccredLog]
}
