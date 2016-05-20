package featurebee.registry.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{S3Object, S3ObjectInputStream}
import com.amazonaws.util.StringInputStream
import featurebee.registry.s3.S3JsonFeatureRegistry.S3File
import org.apache.http.client.methods.HttpRequestBase
import org.mockito.Mockito._
import org.scalatest.{FeatureSpec, MustMatchers, OptionValues}

class S3JsonFeatureRegistrySpec extends FeatureSpec with MustMatchers with OptionValues {

  val jsonConfig =
    s"""
       |[
       |{
       |  "name": "feature-xyz",
       |  "description": "Some additional description 1",
       |  "tags": ["Team Name", "Or Service name"],
       |  "activation": [{"culture": ["de-DE"]}]
       |}
       |]
       """.stripMargin

  feature("Loading a single json feature file from S3") {
    scenario("Loading a present file in a bucket works") {

      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])

      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))
      when(s3Client.getObject("bucket", "key")).thenReturn(s3Obj)

      val r = S3JsonFeatureRegistry.apply(Seq(S3File("bucket", "key")))(s3Client)
      r.isGood must be(true)

      r.foreach {
        registry => registry.feature("feature-xyz").value.featureDescription.name must be("feature-xyz")
      }
    }

    scenario("Loading a missing file returns an Error") {
      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])

      when(s3Client.getObject("bucket", "key")).thenThrow(new RuntimeException("Some ex"))
      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))

      val r = S3JsonFeatureRegistry.apply(Seq(S3File("bucket", "key")))(s3Client)
      r.isBad must be(true)

      r.badMap {
        errors => errors.head must be(S3JsonFeatureRegistry.Error(S3File("bucket", "key"), "java.lang.RuntimeException: Some ex"))
      }
    }
  }
}
