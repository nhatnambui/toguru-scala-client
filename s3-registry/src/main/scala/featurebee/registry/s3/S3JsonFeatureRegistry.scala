package featurebee.registry.s3

import java.time.{LocalDateTime, ZoneId}

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{AmazonClientException, AmazonServiceException}
import featurebee.api.FeatureRegistry
import featurebee.impl.FeatureDescription
import featurebee.json.JsonFeatureRegistry
import org.apache.commons.io.IOUtils
import org.scalactic._

import scala.util.{Failure, Success, Try}


/**
  * S3 Feature registry supports loading of several feature json files from S3.
  * It merges all feature json files and also returns the latest modification date of the set of files.
  */
object S3JsonFeatureRegistry {

  implicit val localDateTimeOrdering = LocalDateTimeOrderingDescending

  case class Error(file: S3File, message: String)

  case class S3File(bucketName: String, key: String, ignoreOnFailures: Boolean = false)

  /**
    * @param featureRegistry the resulting feature registry from all feature json files
    * @param failedIgnoredFiles a list of error descriptions for json files that could be ignored on errors
    * @param lastModified the most recent S3 modification date of the json files
    */
  case class FeatureRegistryBuilt(featureRegistry: FeatureRegistry, failedIgnoredFiles: Seq[Error], lastModified: LocalDateTime)

  def apply(s3Files: Seq[S3File])(implicit amazonS3Client: AmazonS3Client): FeatureRegistryBuilt Or Seq[Error] = {

    case class Accum(featureDescriptions: Seq[FeatureDescription] = Seq.empty, failedAndIgnored: Seq[Error] = Seq.empty, breakingErrors: Seq[Error] = Seq.empty,
                     latestLastModified: Option[LocalDateTime] = None)

    val featureDescriptionsAndErrors: Accum = s3Files.foldLeft(Accum()) {
      (accum, s3File) =>
        val r = contentFromS3(s3File)
        (r, s3File) match {
          case (Bad(f), S3File(bucket, key, true)) => accum.copy(failedAndIgnored = accum.failedAndIgnored :+ f)
          case (Bad(f), S3File(bucket, key, false)) => accum.copy(breakingErrors = accum.breakingErrors :+ f)
          case (Good((json, maybeLastModified)), s3File@ S3File(_, _, ignoreFailures)) =>
            val a = (JsonFeatureRegistry.featureDescriptions(json), ignoreFailures) match {
              case (Good(featureDescriptions), _) => accum.copy(featureDescriptions = accum.featureDescriptions ++ featureDescriptions)
              case (Bad(e), true) => accum.copy(failedAndIgnored = accum.failedAndIgnored :+ Error(s3File, e.head.errorMessage))
              case (Bad(e), false) => accum.copy(breakingErrors = accum.breakingErrors :+ Error(s3File, e.head.errorMessage))
            }
            val newLastModified = Seq(maybeLastModified, a.latestLastModified).flatten.sorted.headOption
            a.copy(latestLastModified = newLastModified)
        }
    }

    featureDescriptionsAndErrors match {
      case Accum(featureDescriptions, ignored, errors, latestLastModified) if errors.isEmpty =>
        Good(FeatureRegistryBuilt(new JsonFeatureRegistry(featureDescriptions), ignored, latestLastModified.getOrElse(LocalDateTime.now())))
      case Accum(_, ignored, errors, _) if errors.nonEmpty => Bad(errors ++ ignored)
    }
  }

  private[s3] def contentFromS3(s3File: S3File)(implicit amazonS3Client: AmazonS3Client): (String, Option[LocalDateTime]) Or Error = {
    Try {
      val s3object = amazonS3Client.getObject(s3File.bucketName, s3File.key)
      val maybeLastModifInstant = Option(s3object.getObjectMetadata.getLastModified).map(_.toInstant).map(i => LocalDateTime.ofInstant(i, ZoneId.systemDefault()))
      (IOUtils.toString(s3object.getObjectContent), maybeLastModifInstant)
    } match {
      case Success((j, m)) =>
        Good((j, m))
      case Failure(ase: AmazonServiceException) =>
        Bad(Error(s3File,
          s"""
             |Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.
             |Error Message:    ${ase.getMessage}
             |HTTP Status Code: ${ase.getStatusCode}
             |AWS Error Code:   ${ase.getErrorCode}
             |Error Type:       ${ase.getErrorType}
             |Request ID:       ${ase.getRequestId}""".stripMargin))

      case Failure(ace: AmazonClientException) =>
        Bad(Error(s3File,
          s"""
             |Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3 such as not being able to access the network.
             |Error Message:    ${ace.getMessage}
            """))
      case Failure(other) => Bad(Error(s3File, s"${other.getClass.getName}: ${other.getMessage}"))
    }
  }

  object LocalDateTimeOrderingDescending extends Ordering[LocalDateTime] {
    override def compare(x: LocalDateTime, y: LocalDateTime): Int = - x.compareTo(y)
  }

}
