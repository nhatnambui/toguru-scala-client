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

  case class S3File(bucketName: String, key: String)

  def apply(s3Files: Seq[S3File])(implicit amazonS3Client: AmazonS3Client): FeatureRegistry Or Every[Error] = {
    val jsonsOrErrors = s3Files.map(s3File => contentFromS3(s3File)).combined
    jsonsOrErrors.map(new JsonFeatureRegistry(_))
  }

  private[s3] def contentFromS3(s3File: S3File)(implicit amazonS3Client: AmazonS3Client): String Or One[Error] = {
    Try {
      val s3object = amazonS3Client.getObject(s3File.bucketName, s3File.key)
      IOUtils.toString(s3object.getObjectContent)
    } match {
      case Success(j) => Good(j)
      case Failure(ase: AmazonServiceException) =>
        Bad(One(Error(s3File,
          s"""
             |Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.
             |Error Message:    ${ase.getMessage}
             |HTTP Status Code: ${ase.getStatusCode}
             |AWS Error Code:   ${ase.getErrorCode}
             |Error Type:       ${ase.getErrorType}
             |Request ID:       ${ase.getRequestId}""".stripMargin)))

      case Failure(ace: AmazonClientException) =>
        Bad(One(Error(s3File,
          s"""
             |Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3 such as not being able to access the network.
             |Error Message:    ${ace.getMessage}
            """)))
      case Failure(other) => Bad(One(Error(s3File, s"${other.getClass.getName}: ${other.getMessage}")))
    }
  }
}
