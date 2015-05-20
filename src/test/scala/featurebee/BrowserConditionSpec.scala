package featurebee

import java.util.Locale

import featurebee.ClientInfo.Browser._
import featurebee.impl.{CultureCondition, BrowserCondition}
import org.scalatest.FeatureSpec

/**
 * @author Chris Wewerka
 */
class BrowserConditionSpec extends FeatureSpec {

  feature("Browser conditions") {

    scenario("Browser condition returns true if browser matches") {
      val clientInfo = ClientInfoImpl(Some(Chrome))
      assert(BrowserCondition(Set(Chrome)).applies(clientInfo))
    }

    scenario("Browser condition returns false if browser does not match") {
      val clientInfo = ClientInfoImpl(Some(Chrome))
      assert(BrowserCondition(Set(Ie)).applies(clientInfo) === false)
    }
  }

  feature("Locale/Culture conditions") {
    scenario("de-DE locale correctly applies") {
      val clientInfo = ClientInfoImpl(culture = Some(Locale.GERMANY))
      assert(CultureCondition(Set(Locale.GERMANY)).applies(clientInfo))
    }

    scenario("Culture condition for lang german applies to germany locale de-DE from client") {
      val clientInfo = ClientInfoImpl(culture = Some(Locale.GERMANY))
      assert(CultureCondition(Set(new Locale("", "DE"))).applies(clientInfo))
    }
  }
}
