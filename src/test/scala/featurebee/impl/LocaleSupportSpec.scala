package featurebee.impl

import java.util.Locale

import org.scalatest.{FeatureSpec, FunSuite}

class LocaleSupportSpec extends FeatureSpec {

  import LocaleSupport.RichLocale

  feature("Country extraction from locale") {
    scenario("Country gets successfully extracted") {
      assert(Locale.GERMANY.country === Some("DE"))
    }

    scenario("Unset country returns null for extraction") {
      assert(new Locale("", "").country === None)
    }
  }

  feature("Lang extraction from locale") {
    scenario("Lang gets successfully extracted") {
      assert(Locale.GERMANY.lang === Some("de"))
    }

    scenario("Unset lang returns null for extraction") {
      assert(new Locale("", "").lang === None)
    }
  }

}
