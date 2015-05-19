package featurebee.json

import org.scalatest.FunSuite

class StaticJsonFeatureRegistrySuite extends FunSuite {

  test("Creating Static Json Feature registry from file in classpath") {
    val featureReg = StaticJsonFeatureRegistry("feature-config-sample.txt")
    assert(featureReg.feature("Name of the Feature").nonEmpty)
  }
}
