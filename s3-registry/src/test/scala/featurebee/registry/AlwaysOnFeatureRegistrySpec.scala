package featurebee.registry

import featurebee.ClientInfoImpl
import org.scalatest.{MustMatchers, OptionValues, FeatureSpec}

class AlwaysOnFeatureRegistrySpec extends FeatureSpec with OptionValues with MustMatchers {

  feature("Features are always toggled on for AlwaysOnFeatureRegistry") {
    scenario("'xyz' feature is toggled on") {
      AlwaysOnFeatureRegistry.feature("xyz").value.isActive(ClientInfoImpl()) must be(true)
    }
  }

  feature("feature names") {
    scenario("return empty set") {
      AlwaysOnFeatureRegistry.allFeatures must be(Set.empty)
    }
  }
}
