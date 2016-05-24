package featurebee.registry

import org.scalatest.{MustMatchers, FeatureSpec, FunSuite}

class DefaultFeatureValueFeatureRegistrySpec extends FeatureSpec with MustMatchers{

  feature("Feature for a given name") {
    scenario("Is always None") {
      DefaultFeatureValueFeatureRegistry.feature("xyz") must be(None)
    }
  }
}
