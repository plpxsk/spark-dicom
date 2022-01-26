package ai.kaiko.dicom

import scala.util.Try
import scala.xml.XML
import org.dcm4che3.data.Keyword
import org.dcm4che3.data.VR
import org.dcm4che3.data.VR._

import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class DicomDeidElem(
  tag: Int,
  name: String,
  keyword: String,
  action: String
)

object DicomDeidentifyDictionary {

  val DUMMY_DATE = LocalDate.of(1, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)
  val DUMMY_TIME = LocalTime.of(0, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_TIME)
  val DUMMY_DATE_TIME = LocalDateTime.of(LocalDate.of(1, 1, 1), LocalTime.of(0, 0, 0, 0)).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  val ZERO_STRING = "0"
  val ZERO_INT = 0
  val EMPTY_STRING = ""
  val DUMMY_STRING = "Anonymized"

  val DICOM_DEID_XML_DOC_FILEPATH =
    "/dicom/stdDoc/part15.xml"

  lazy val elements: Array[DicomDeidElem] = {
    val xmlResourceInputStream =
      Option(
        DicomDeidentifyDictionary.getClass.getResourceAsStream(
          DICOM_DEID_XML_DOC_FILEPATH
        )
      ).get
      val dicomDeidXmlDoc = XML.load(xmlResourceInputStream)
      // find relevant xml table holding dict
      ((dicomDeidXmlDoc \\ "table" filter (elem =>
        elem \@ "label" == "E.1-1"
      )) \ "tbody" \ "tr")
      // to Map entries
      .map(row => {
        // there is an invisible space in the texts, remove it
        val rowCellTexts = row \ "td" map (_.text.trim.replaceAll("​", ""))
        // we'll keep only std elements with valid hexadecimal tag
        Try(
          Integer.parseInt(
            rowCellTexts(1)
              .replace("(", "")
              .replace(")", "")
              .replace(",", ""),
            16
          )
        ).toOption.map(intTag =>
          DicomDeidElem(
            tag = intTag,
            name = rowCellTexts(0),
            keyword = Keyword.valueOf(intTag),
            action = rowCellTexts(4)
          )
        )
      })
      .collect { case Some(v) if v.name.nonEmpty => v }
      .toArray
  }

  lazy val keywordMap: Map[String, DicomDeidElem] =
    elements.map(deidElem => deidElem.keyword -> deidElem).toMap

  lazy val tagMap: Map[Int, DicomDeidElem] =
    elements.map(deidElem => deidElem.tag -> deidElem).toMap

  def getDummyValue(vr: VR): Option[Any] = {
    vr match {
      case LO | SH | PN | CS => Some(DUMMY_STRING)
      case DA => Some(DUMMY_DATE)
      case TM => Some(DUMMY_TIME)
      case DT => Some(DUMMY_DATE_TIME)
      case IS => Some(ZERO_STRING)
      case FD | FL | SS | US => Some(ZERO_INT)
      case ST => Some(EMPTY_STRING)
      case _ => None
    }
  }

  def getEmptyValue(vr: VR): Option[Any] = {
    vr match {
      case SH | PN | UI | LO | CS => Some(EMPTY_STRING)
      case DA => Some(DUMMY_DATE)
      case TM => Some(DUMMY_TIME)
      case UL => Some(ZERO_INT)
      case _ => None
    }
  }
}