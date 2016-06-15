package featurebee.registry.s3

import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ObjectMetadata, S3Object, S3ObjectInputStream}
import com.amazonaws.util.StringInputStream
import featurebee.registry.s3.S3JsonFeatureRegistry.{Error, S3File}
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

  val invalidJsonConfig =
    s"""
       |[
       |{
       |  "name": "feature-xyz",
       |  "description": "Some additional description 1",
       |  "tags": ["Team Name", "Or Service name"],
       |  "activation": [{"culture": ["de-DE"]}],
       |}
       |]
       """.stripMargin

  feature("Loading a single json feature with errors from S3") {
    scenario("Loading a present file that contains syntactic errors works but returns a Bad(error)") {

      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])
      val s3Meta = mock(classOf[ObjectMetadata])

      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(invalidJsonConfig), mock(classOf[HttpRequestBase])))
      when(s3Obj.getObjectMetadata).thenReturn(s3Meta)
      when(s3Client.getObject("bucket", "key")).thenReturn(s3Obj)

      val r = S3JsonFeatureRegistry.apply(Seq(S3File("bucket", "key")))(s3Client)
      r.isBad must be(true)

      r.swap.foreach {
        errors =>
          errors.size must be(1)
          errors.headOption.value.file must be(S3File("bucket", "key", ignoreOnFailures = false))
          errors.headOption.value.message must startWith("ParsingException: Unexpected character '}' at input index ")
      }
    }
  }

  feature("Loading a single json feature file from S3") {
    scenario("Loading a present file in a bucket works") {

      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])
      val s3Meta = mock(classOf[ObjectMetadata])

      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))
      when(s3Obj.getObjectMetadata).thenReturn(s3Meta)
      when(s3Client.getObject("bucket", "key")).thenReturn(s3Obj)

      val r = S3JsonFeatureRegistry.apply(Seq(S3File("bucket", "key")))(s3Client)
      r.isGood must be(true)

      r.foreach {
        registryBuilt => registryBuilt.featureRegistry.feature("feature-xyz").value.featureDescription.name must be("feature-xyz")
      }
    }
  }

  feature("Loading a multiple json feature files from S3") {

    scenario("Loading a missing mandatory file returns an Error") {
      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])
      val s3Meta = mock(classOf[ObjectMetadata])

      when(s3Client.getObject("bucket", "key1")).thenReturn(s3Obj)
      when(s3Client.getObject("bucket", "key2")).thenThrow(new RuntimeException("Some ex"))
      when(s3Obj.getObjectMetadata).thenReturn(s3Meta)
      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))

      val r = S3JsonFeatureRegistry.apply(Seq(
        S3File("bucket", "key1", ignoreOnFailures = false),
        S3File("bucket", "key2", ignoreOnFailures = false)
      ))(s3Client)
      r.isBad must be(true)

      r.badMap {
        errors => errors.head must be(S3JsonFeatureRegistry.Error(S3File("bucket", "key2"), "java.lang.RuntimeException: Some ex"))
      }
    }

    scenario("Loading a missing optional file returns no error") {
      val s3Client = mock(classOf[AmazonS3Client])
      val s3Obj = mock(classOf[S3Object])
      val s3Meta = mock(classOf[ObjectMetadata])

      when(s3Client.getObject("bucket", "key1")).thenReturn(s3Obj)
      when(s3Client.getObject("bucket", "key2")).thenThrow(new RuntimeException("Some ex"))
      when(s3Obj.getObjectMetadata).thenReturn(s3Meta)
      when(s3Obj.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))

      val r = S3JsonFeatureRegistry.apply(Seq(
        S3File("bucket", "key1", ignoreOnFailures = false),
        S3File("bucket", "key2", ignoreOnFailures = true)
      ))(s3Client)
      r.isGood must be(true)

      r.foreach {
        registryBuilt => registryBuilt.featureRegistry.feature("feature-xyz").value.featureDescription.name must be("feature-xyz")
      }
    }

    scenario("Loading multiple files from a bucket works") {

      val s3Client = mock(classOf[AmazonS3Client])

      val s3Obj1 = mock(classOf[S3Object])
      val s3Obj2 = mock(classOf[S3Object])
      val s3Meta1 = mock(classOf[ObjectMetadata])
      val s3Meta2 = mock(classOf[ObjectMetadata])

      when(s3Obj1.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))
      when(s3Obj2.getObjectContent).thenReturn(new S3ObjectInputStream(new StringInputStream(jsonConfig), mock(classOf[HttpRequestBase])))
      when(s3Obj1.getObjectMetadata).thenReturn(s3Meta1)
      when(s3Obj2.getObjectMetadata).thenReturn(s3Meta2)

      val earlierDate = new Date()
      val laterDate = new Date(earlierDate.getTime + 1000)
      when(s3Meta1.getLastModified).thenReturn(earlierDate)
      when(s3Meta2.getLastModified).thenReturn(laterDate)

      when(s3Client.getObject("bucket", "key1")).thenReturn(s3Obj1)
      when(s3Client.getObject("bucket", "key2")).thenReturn(s3Obj2)

      val r = S3JsonFeatureRegistry.apply(Seq(
        S3File("bucket", "key1"),
        S3File("bucket", "key2")
      ))(s3Client)
      r.isGood must be(true)

      r.foreach {
        registryBuilt =>
          registryBuilt.featureRegistry.feature("feature-xyz").value.featureDescription.name must be("feature-xyz")
          registryBuilt.lastModified must be(LocalDateTime.ofInstant(laterDate.toInstant, ZoneId.systemDefault()))
      }
    }
  }
}
