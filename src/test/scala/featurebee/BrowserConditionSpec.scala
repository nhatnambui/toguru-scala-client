package featurebee

import featurebee.ClientInfo.Browser._
import org.scalatest.FeatureSpec

/**
 * @author Chris Wewerka
 */
class BrowserConditionSpec extends FeatureSpec {

  feature("Browser conditions") {

    scenario("Browser condition returns true if browser matches") {
      val clientInfo = ClientInfoImpl(Some(Chrome))
      assert(BrowserCondition(Set(Chrome)).applies(clientInfo) === true)
    }

    scenario("Browser condition returns false if browser does not match") {
      val clientInfo = ClientInfoImpl(Some(Chrome))
      assert(BrowserCondition(Set(Ie)).applies(clientInfo) === false)
    }
  }

  // TODO other conditions tests
}
