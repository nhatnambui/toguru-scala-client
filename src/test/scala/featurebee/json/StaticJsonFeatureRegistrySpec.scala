package featurebee.json

import java.util.Locale

import featurebee.api.Feature
import featurebee.helpers.ClientInfoHelper
import featurebee.{ClientInfoImpl, ClientInfo}
import org.scalatest.{ShouldMatchers, FeatureSpec, FunSuite}
import ClientInfoHelper._

import scala.None

class StaticJsonFeatureRegistrySpec extends FeatureSpec with ShouldMatchers {

  val featureReg = StaticJsonFeatureRegistry("feature-config-sample.txt")

  scenario("specific feature from static Json Feature registry from file in classpath") {
    assert(featureReg.feature("Name of the Feature").nonEmpty)
  }

  scenario("all features Creating Static Json Feature registry from file in classpath") {
    assert(featureReg.allFeatures.size === 1)
  }

  scenario("duplicate feature names throws IllegalStateException") {

    val invalidJsonConfig =
      s"""
         |[
         |{
         |  "name": "DuplicateName",
         |  "description": "Some additional description",
         |  "tags": ["Team Name", "Or Service name"],
         |  "activation": [{"culture": ["de-DE"]}]
         |},
         |{
         |  "name": "DuplicateName",
         |  "description": "Some additional description",
         |  "tags": ["Team Name", "Or Service name"],
         |  "activation": [{"culture": ["de-DE"]}]
         |}
         |]
       """.stripMargin

    intercept[IllegalStateException] {
      new StaticJsonFeatureRegistry(invalidJsonConfig)
    }
  }


  feature("Provide Feature Query Strings for Service Fragments") {

    scenario("There are no services defined for a feature") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": true}]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl()

      sut.featureStringForService("content-service") should be("")
    }

    scenario("There are no features for a service") {
      val input = """[{
        |  "name": "name-of-feature",
        |  "description": "Some additional description",
        |  "tags": ["Team Name", "Or Service name"],
        |  "activation": [{"default": true}],
        |  "services": []
        |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl()

      sut.featureStringForService("content-service") should be("")
    }

    scenario("There is a feature for a service and it is enabled by default") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": true}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl()

      sut.featureStringForService("content-service") should be("name-of-feature=true")
    }

    scenario("There is a feature for a service and it is disabled by default") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": false}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl()

      sut.featureStringForService("content-service") should be("name-of-feature=false")
    }

    scenario("There is a feature for a service and it is enabled by the client") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": false}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl(None, None, None, forceFeatureTo("name-of-feature", enabled = true))

      sut.featureStringForService("content-service") should be("name-of-feature=true")
    }

    scenario("There is a feature for a service and it is disabled by the client") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": true}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl(None, None, None, forceFeatureTo("name-of-feature", enabled = false))

      sut.featureStringForService("content-service") should be("name-of-feature=false")
    }

    scenario("There is a feature for a service and it is enabled by the client locale") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"culture": ["de-DE"]}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl(None, Some(new Locale("de", "DE")))

      sut.featureStringForService("content-service") should be("name-of-feature=true")
    }

    scenario("There is a feature for a service and it is disabled by the client locale") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"culture": ["fr-BE"]}],
                    |  "services": ["content-service"]
                    |}]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl(None, Some(new Locale("de-DE")))

      sut.featureStringForService("content-service") should be("name-of-feature=false")
    }

    scenario("There are multiple features for a service") {
      val input = """[{
                    |  "name": "name-of-feature",
                    |  "description": "Some additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": true}],
                    |  "services": ["content-service"]
                    |},
                    |{
                    |  "name": "name-of-feature-2",
                    |  "description": "Some other additional description",
                    |  "tags": ["Team Name", "Or Service name"],
                    |  "activation": [{"default": true}],
                    |  "services": ["content-service"]
                    |}
                    |]""".stripMargin

      val sut = new StaticJsonFeatureRegistry(input)
      implicit val clientInfo: ClientInfo = ClientInfoImpl()

      sut.featureStringForService("content-service") should be("name-of-feature=true|name-of-feature-2=true")
    }
  }
}
