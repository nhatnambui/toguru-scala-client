package featurebee.json

import org.scalatest.FunSuite

class StaticJsonFeatureRegistrySuite extends FunSuite {

  val featureReg = StaticJsonFeatureRegistry("feature-config-sample.txt")

  test("specific feature from static Json Feature registry from file in classpath") {
    assert(featureReg.feature("Name of the Feature").nonEmpty)
  }

  test("all features Creating Static Json Feature registry from file in classpath") {
    assert(featureReg.allFeatures.size === 1)
  }
}
