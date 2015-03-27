package com.featurebee

import org.scalatest._

class FeatureBeeSpec extends FlatSpec with MustMatchers {
  "FeatureBee" should "have tests" in {
    FeatureBee.isActive("any") must be === false
  }
}
