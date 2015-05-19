package featurebee.json

import featurebee.api.Feature
import org.scalatest.FunSuite
import org.scalatest.OptionValues._

class StaticJsonFeatureRegistrySuite extends FunSuite {

  test("Creating Static Json Feature registry from file in classpath") {
    val featureReg = StaticJsonFeatureRegistry("feature-config-sample.txt")
    assert(featureReg.feature("Name of the Feature").nonEmpty)
  }
}
