package deidentifier

import ai.kaiko.spark.dicom.deidentifier.DicomDeidentifier._
import ai.kaiko.dicom.DicomDeidentifyDictionary.{
  DUMMY_DATE,
  DUMMY_TIME,
  DUMMY_DATE_TIME,
  ZERO_STRING,
  ZERO_INT,
  EMPTY_STRING,
  DUMMY_STRING
}
import org.apache.log4j.Level
import org.apache.log4j.LogManager

import org.apache.spark.sql.SparkSession

import org.dcm4che3.data.Keyword.{valueOf => keywordOf}
import org.dcm4che3.data._

import org.scalatest.BeforeAndAfterAll
import org.scalatest.CancelAfterFailure
import org.scalatest.funspec.AnyFunSpec

import java.io.File

trait WithSpark {
  var spark = {
    val spark = SparkSession.builder.master("local").getOrCreate
    spark.sparkContext.setLogLevel(Level.ERROR.toString())
    spark
  }
}

object TestDicomDeidentifier {
  val SOME_DICOM_FILEPATH =
    "src/test/resources/Pancreatic-CT-CBCT-SEG/Pancreas-CT-CB_001/07-06-2012-NA-PANCREAS-59677/201.000000-PANCREAS DI iDose 3-97846/1-001.dcm"
  lazy val SOME_DICOM_FILE = {
    val file = new File(SOME_DICOM_FILEPATH)
    assert(file.exists)
    file
  }
}

class TestDicomDeidentifier
    extends AnyFunSpec
    with WithSpark
    with BeforeAndAfterAll
    with CancelAfterFailure {
  import TestDicomDeidentifier._

  val logger = {
    val logger = LogManager.getLogger(getClass.getName);
    logger.setLevel(Level.DEBUG)
    logger
  }

  override protected def afterAll(): Unit = {
    spark.stop
  }

  val SOME_DROPPED_COLUMN = "ContainerComponentID"

  describe("Spark") {
    it("Deidentify DICOM DataFrame") {
      var df = spark.read
        .format("dicomFile")
        .load(SOME_DICOM_FILEPATH)

      df = deidentify(df)

      val row = df.first

      assert(
        row.getAs[String](keywordOf(Tag.ContentDate))
          === DUMMY_DATE
      )
      assert(
        row.getAs[String](keywordOf(Tag.ContentTime))
          === DUMMY_TIME
      )
      assert(
        row.getAs[String](keywordOf(Tag.AttributeModificationDateTime))
          === DUMMY_DATE_TIME
      )
      assert(
        row.getAs[String](keywordOf(Tag.AnnotationGroupLabel))
          === DUMMY_STRING
      )
      // assert(
      //   row.getAs[String](keywordOf(Tag.SimpleFrameList))
      //     === ZERO_INT
      // )
      // assert(
      //   row.getAs[String](keywordOf(Tag.AnnotationGroupLabel))
      //     === ZERO_STRING
      // )
      // assert(
      //   row.getAs[String](keywordOf(Tag.AnnotationGroupLabel))
      //     === EMPTY_STRING
      // )
      assertThrows[IllegalArgumentException]{
        row.fieldIndex(SOME_DROPPED_COLUMN)
      }
    }
  }
}