package ai.kaiko.spark.dicom.v2

import org.apache.spark.sql.connector.catalog.SupportsRead
import org.apache.spark.sql.connector.catalog.TableCapability
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.{util => ju}
import scala.collection.JavaConverters
import scala.collection.mutable
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.FileFormat
import org.apache.spark.sql.execution.datasources.v2.FileTable
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.hadoop.fs.FileStatus
import ai.kaiko.spark.dicom.DicomFileFormat

case class DicomTable(
    name: String,
    sparkSession: SparkSession,
    options: CaseInsensitiveStringMap,
    paths: Seq[String],
    userSpecifiedSchema: Option[StructType],
    fallbackFileFormat: Class[_ <: FileFormat]
) extends FileTable(sparkSession, options, paths, userSpecifiedSchema) {

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder =
    DicomScanBuilder(sparkSession, fileIndex, schema, dataSchema, options)

  override def inferSchema(files: Seq[FileStatus]): Option[StructType] =
    Some(DicomFileFormat.SCHEMA)

  override def newWriteBuilder(x$1: LogicalWriteInfo): WriteBuilder = ???

  override def formatName: String = "DICOM"
}