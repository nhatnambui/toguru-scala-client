package featurebee

import org.scalatest.{FeatureSpec, FunSpec, FunSuite}
import org.scalatest.OptionValues._

class FeaturesStringSpec extends FeatureSpec {

  feature("Parsing of forced features string") {

    val forcedFeaturesString = "feature1=true|feature2=false|feature3=true"
    val forcedFeatures = FeaturesString.parseForcedFeaturesString(forcedFeaturesString)

    scenario("Existing features are correctly forced") {
      assert(forcedFeatures("feature1").value === true)
      assert(forcedFeatures("feature2").value === false)
      assert(forcedFeatures("feature3").value === true)
    }

    scenario("Non existant forced features return None") {
      assert(forcedFeatures("i do not exist") === None)
    }

    scenario("Feature forcing is case insensitive") {
      assert(forcedFeatures("FEATURE1").value === true)
    }

    scenario("Illegal feature spec returns None for feature specified wrongly") {
      val forcedFeaturesString = "feature1=thisiswrong|feature2=false|feature3=true"
      val forcedFeatures = FeaturesString.parseForcedFeaturesString(forcedFeaturesString)

      assert(forcedFeatures("feature1") === None)
      assert(forcedFeatures("feature3").value === true)
    }
  }

}
