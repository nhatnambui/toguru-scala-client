package com.featurebee

import org.scalatest.FeatureSpec
import ClientInfo.Browser._
import org.scalatest.OptionValues._

/**
 * @author Chris Wewerka
 */
class BrowserConditionSpec extends FeatureSpec {

  feature("Browser conditions") {
    scenario("Browser condition returns true if browser matches") {
      val clientInfo = ClientInfoImpl(Some(Chrome), None, (_) => Some(false))
      assert(BrowserCondition(Set(Chrome)).applies(clientInfo) === true)
    }
  }
}
