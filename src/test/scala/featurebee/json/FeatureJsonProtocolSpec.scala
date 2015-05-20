package featurebee.json

import java.util.Locale

import featurebee.ClientInfo.Browser
import featurebee.impl.{UuidDistributionCondition, BrowserCondition, CultureCondition, FeatureDescription}
import org.scalatest.{FeatureSpec, FunSuite}
import FeatureJsonProtocol._
import spray.json.DefaultJsonProtocol
import spray.json._

class FeatureJsonProtocolSpec extends FeatureSpec {

  val sampleJsonChromeDE = """[{
                     |  "name": "Name of the Feature",
                     |  "description": "Some additional description",
                     |  "tags": ["Team Name", "Or Service name"],
                     |  "state": "inProgress",
                     |  "conditions": [{"culture": ["de-DE"]}, {"browser": ["Chrome"]}]
                     |}]""".stripMargin

  val sampleJsonFirefoxEN = """[{
                     |  "name": "Name of the Feature",
                     |  "description": "Some additional description",
                     |  "tags": ["Team Name", "Or Service name"],
                     |  "state": "inProgress",
                     |  "conditions": [{"culture": ["en"]}, {"browser": ["Firefox"]}]
                     |}]""".stripMargin

  val sampleJsonUuidDistributionArray = """[{
                              |  "name": "Name of the Feature",
                              |  "description": "Some additional description",
                              |  "tags": ["Team Name", "Or Service name"],
                              |  "state": "inProgress",
                              |  "conditions": [{"uuidDistribution": ["5-10", "10-20"]}]
                              |}]""".stripMargin

  val sampleJsonUuidDistributionSingleRange = """[{
                                          |  "name": "Name of the Feature",
                                          |  "description": "Some additional description",
                                          |  "tags": ["Team Name", "Or Service name"],
                                          |  "state": "inProgress",
                                          |  "conditions": [{"uuidDistribution": "5-10"}]
                                          |}]""".stripMargin

  feature("Parsing of feature desc json") {
    scenario("Successfully parse sample json from story desc") {
      val featureDescs = sampleJsonChromeDE.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.name === "Name of the Feature")
      assert(featureDescs.head.conditions.size === 2)
      assert(featureDescs.head.conditions.head === CultureCondition(Set(new Locale("de", "DE"))))
      assert(featureDescs.head.conditions.tail.head === BrowserCondition(Set(Browser.Chrome)))
    }

    scenario("Successfully parse sample json with language only locale") {
      val featureDescs = sampleJsonFirefoxEN.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.name === "Name of the Feature")
      assert(featureDescs.head.conditions.size === 2)
      assert(featureDescs.head.conditions.head === CultureCondition(Set(new Locale("en"))))
      assert(featureDescs.head.conditions.tail.head === BrowserCondition(Set(Browser.Firefox)))
    }

    scenario("Successfully parse sample json with 2 uuid distributions") {
      val featureDescs = sampleJsonUuidDistributionArray.parseJson.convertTo[Seq[FeatureDescription]]
      // assert(featureDescs.head.conditions.size === 2)
      assert(featureDescs.head.conditions.head ===
        UuidDistributionCondition(Seq(5 to 10, 10 to 20), UuidDistributionCondition.defaultUuidToIntProjection))
    }

    scenario("Successfully parse sample json with uuid distribution single range") {
      val featureDescs = sampleJsonUuidDistributionSingleRange.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.head.conditions.size === 1)
      assert(featureDescs.head.conditions.head === UuidDistributionCondition(5 to 10))
    }
  }
}
