package featurebee.registry.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{AmazonClientException, AmazonServiceException}
import featurebee.api.FeatureRegistry
import featurebee.json.JsonFeatureRegistry
import org.apache.commons.io.IOUtils
import org.scalactic.Accumulation._
import org.scalactic._

import scala.util.{Failure, Success, Try}

object S3JsonFeatureRegistry {

  case class Error(file: S3File, message: String)

  case class S3File(bucketName: String, key: String, ignoreOnFailures: Boolean = false)

  case class FeatureRegistryBuilt(featureRegistry: FeatureRegistry, failedIgnoredFiles: Seq[Error])

  def apply(s3Files: Seq[S3File])(implicit amazonS3Client: AmazonS3Client): FeatureRegistryBuilt Or Seq[Error] = {

    case class Accum(jsons: Seq[String] = Seq.empty, failedAndIgnored:Seq[Error] = Seq.empty, breakingErrors: Seq[Error] = Seq.empty)

    val jsonsAndErrors: Accum = s3Files.foldLeft(Accum()){
      (accum, s3File) =>
        val r = contentFromS3(s3File)
        (r, s3File) match {
          case (Bad(f), S3File(bucket, key, true)) => accum.copy(failedAndIgnored = accum.failedAndIgnored :+ f)
          case (Bad(f), S3File(bucket, key, false)) => accum.copy(breakingErrors = accum.breakingErrors :+ f)
          case (Good(json), S3File(_, _, _)) => accum.copy(jsons = accum.jsons :+ json)
        }
    }

    jsonsAndErrors match {
      case Accum(jsons, ignored, errors) if errors.isEmpty => Good(FeatureRegistryBuilt(new JsonFeatureRegistry(jsons), ignored))
      case Accum(jsons, ignored, errors) if errors.nonEmpty => Bad(errors)
    }
  }

  private[s3] def contentFromS3(s3File: S3File)(implicit amazonS3Client: AmazonS3Client): String Or Error = {
    Try {
      val s3object = amazonS3Client.getObject(s3File.bucketName, s3File.key)
      IOUtils.toString(s3object.getObjectContent)
    } match {
      case Success(j) => Good(j)
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
}
