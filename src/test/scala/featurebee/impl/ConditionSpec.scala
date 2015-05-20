package featurebee.impl

import java.util.{UUID, Locale}

import featurebee.ClientInfo.Browser._
import featurebee.ClientInfoImpl
import org.scalatest.FeatureSpec

class ConditionSpec extends FeatureSpec {

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

  feature("Uuid distribution conditions") {
    val uuidWithDefaultProjectionToFive = UUID.fromString("56ed135b-a474-41d4-bde1-bc5e1e8bf910")

    scenario("Uuid distribution applies to true if uuid is projected into the given range") {
      val clientInfo = ClientInfoImpl(uuid = Some(uuidWithDefaultProjectionToFive))
      assert(UuidDistributionCondition(3 to 5).applies(clientInfo))
    }

    scenario("Uuid distribution applies to false if uuid is projected outside the given range") {
      val clientInfo = ClientInfoImpl(uuid = Some(uuidWithDefaultProjectionToFive))
      assert(!UuidDistributionCondition(10 to 11).applies(clientInfo))
    }
  }

  feature("default uuid to in projection") {
    val uuid = UUID.randomUUID()
    scenario(s"Random uuid ($uuid) gets projected") {
      println(uuid)
      val projected = UuidDistributionCondition.defaultUuidToIntProjection(uuid)
      println(projected)
      assert(projected > 0 && projected <= 100)
    }
  }
}
