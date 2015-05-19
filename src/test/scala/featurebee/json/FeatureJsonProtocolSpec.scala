package featurebee.json

import java.util.Locale

import featurebee.ClientInfo.Browser
import featurebee.impl.{BrowserCondition, CultureCondition, FeatureDescription}
import org.scalatest.{FeatureSpec, FunSuite}
import FeatureJsonProtocol._
import spray.json.DefaultJsonProtocol
import spray.json._

class FeatureJsonProtocolSpec extends FeatureSpec {

  val sampleJson = """[{
                     |  "name": "Name of the Feature",
                     |  "description": "Some additional description",
                     |  "tags": ["Team Name", "Or Service name"],
                     |  "state": "inDevelopment",
                     |  "conditions": [{"culture": ["de-DE"]}, {"browser": ["Chrome"]}]
                     |}]""".stripMargin

  feature("Parsing of feature desc json") {
    scenario("Successfully parse sample json from story desc") {
      val featureDescs = sampleJson.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.name === "Name of the Feature")
      assert(featureDescs.head.conditions.size === 2)
      assert(featureDescs.head.conditions.head === CultureCondition(Set(new Locale("de", "DE"))))
      assert(featureDescs.head.conditions.tail.head === BrowserCondition(Set(Browser.Chrome)))
    }
  }
}
