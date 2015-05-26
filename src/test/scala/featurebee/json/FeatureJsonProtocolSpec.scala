package featurebee.json

import java.util.Locale

import featurebee.impl._
import featurebee.json.FeatureJsonProtocol._
import org.scalatest.FeatureSpec
import spray.json._
import org.scalatest.OptionValues._

class FeatureJsonProtocolSpec extends FeatureSpec {

  val sampleJsonChromeDE = """[{
                     |  "name": "Name of the Feature",
                     |  "description": "Some additional description",
                     |  "tags": ["Team Name", "Or Service name"],
                     |  "activation": [{"culture": ["de-DE"]}, {"userAgentFragments": ["Chrome"]}]
                     |}]""".stripMargin

  val sampleJsonFirefoxEN = """[{
                     |  "name": "Name of the Feature",
                     |  "description": "Some additional description",
                     |  "tags": ["Team Name", "Or Service name"],
                     |  "activation": [{"culture": ["en"]}, {"userAgentFragments": ["Firefox"]}]
                     |}]""".stripMargin

  val sampleJsonUuidDistributionArray = """[{
                              |  "name": "Name of the Feature",
                              |  "description": "Some additional description",
                              |  "tags": ["Team Name", "Or Service name"],
                              |  "activation": [{"trafficDistribution": ["5-10", "10-20"]}]
                              |}]""".stripMargin

  val sampleJsonUuidDistributionSingleRange = """[{
                                          |  "name": "Name of the Feature",
                                          |  "description": "Some additional description",
                                          |  "tags": ["Team Name", "Or Service name"],
                                          |  "activation": [{"trafficDistribution": "96-100"}]
                                          |}]""".stripMargin

  feature("Parsing of feature desc json") {
    scenario("Successfully parse sample json from story desc") {
      val featureDescs = sampleJsonChromeDE.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.name === "Name of the Feature")
      assert(featureDescs.head.activation.size === 2)
      assert(featureDescs.head.activation.head === CultureCondition(Set(new Locale("de", "DE"))))
      assert(featureDescs.head.activation.tail.head === UserAgentCondition(Set("Chrome")))
    }

    scenario("Successfully parse sample json with language only locale") {
      val featureDescs = sampleJsonFirefoxEN.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.name === "Name of the Feature")
      assert(featureDescs.head.activation.size === 2)
      assert(featureDescs.head.activation.head === CultureCondition(Set(new Locale("en"))))
      assert(featureDescs.head.activation.tail.head === UserAgentCondition(Set("Firefox")))
    }

    scenario("Successfully parse sample json with 2 uuid distributions") {
      val featureDescs = sampleJsonUuidDistributionArray.parseJson.convertTo[Seq[FeatureDescription]]
      // assert(featureDescs.head.conditions.size === 2)
      assert(featureDescs.head.activation.head ===
        UuidDistributionCondition(Seq(5 to 10, 10 to 20), UuidDistributionCondition.defaultUuidToIntProjection))
    }

    scenario("Successfully parse sample json with uuid distribution single range") {
      val featureDescs = sampleJsonUuidDistributionSingleRange.parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.head.activation.size === 1)
      assert(featureDescs.head.activation.head === UuidDistributionCondition(96 to 100))
    }

    scenario("Invalid range throws DeserializationEx") {
      intercept[DeserializationException] {
        FeatureJsonProtocol.ConditionJsonFormat.parseToRange("10-5")
      }
    }
  }

  feature("Default condition mapping") {
    def sampleJsonDefaultActivation(defaultCondValue: String) = s"""[{
                                        |  "name": "Name of the Feature",
                                        |  "description": "Some additional description",
                                        |  "tags": ["Team Name", "Or Service name"],
                                        |  "activation": [{"default": $defaultCondValue}]
                                        |}]""".stripMargin

    scenario("Parsing of feature desc with default condition with boolean set to true") {
      val featureDescs = sampleJsonDefaultActivation("true").parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.activation === Set(AlwaysOnCondition))
    }

    scenario("Parsing of feature desc with default condition with boolean set to false") {
      val featureDescs = sampleJsonDefaultActivation("false").parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.activation === Set(AlwaysOffCondition))
    }

    scenario("Parsing of feature desc with default condition with string value 'on'") {
      val featureDescs = sampleJsonDefaultActivation(""" "on" """).parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.activation === Set(AlwaysOnCondition))
    }

    scenario("Parsing of feature desc with default condition with string value 'off'") {
      val featureDescs = sampleJsonDefaultActivation(""" "off" """).parseJson.convertTo[Seq[FeatureDescription]]
      assert(featureDescs.size === 1)
      assert(featureDescs.head.activation === Set(AlwaysOffCondition))
    }

    scenario("Parsing of feature desc with default condition with invalid int value 1 causes deserialization exception") {
      val sampleJsonDefaultActivation = """[{
                                          |  "name": "Name of the Feature",
                                          |  "description": "Some additional description",
                                          |  "tags": ["Team Name", "Or Service name"],
                                          |  "activation": [{"default": 1 }]
                                          |}]""".stripMargin
      intercept[DeserializationException] {
        val featureDescs = sampleJsonDefaultActivation.parseJson.convertTo[Seq[FeatureDescription]]
      }
    }
  }

  feature("Locale string mapping") {
    scenario("Map locale de-DE") {
      val l = FeatureJsonProtocol.ConditionJsonFormat.mapLocale("de-DE")
      assert(l === Locale.GERMANY)
    }

    scenario("Map lang only locale de") {
      val l = FeatureJsonProtocol.ConditionJsonFormat.mapLocale("de")
      assert(l === Locale.GERMAN)
    }

    scenario("Map country only locale DE") {
      import featurebee.impl.LocaleSupport._
      val l = FeatureJsonProtocol.ConditionJsonFormat.mapLocale("DE")
      assert(l.country.value === "DE")
    }

    scenario("Invalid locale string causes DeserializationEx") {
      intercept[DeserializationException] {
        FeatureJsonProtocol.ConditionJsonFormat.mapLocale("de_DE")
      }
    }
  }

  feature("Unsupported conditions throws DeserializationException") {
    scenario("Unsupported conditions throws DeserializationException") {

      val unknownCond = """[{
                                  |  "name": "Name of the Feature",
                                  |  "description": "Some additional description",
                                  |  "tags": ["Team Name", "Or Service name"],
                                  |  "activation": [{"unknown-condition": ""}]
                                  |}]""".stripMargin

      intercept[DeserializationException] {
        unknownCond.parseJson.convertTo[Seq[FeatureDescription]]
      }
    }
  }
}
