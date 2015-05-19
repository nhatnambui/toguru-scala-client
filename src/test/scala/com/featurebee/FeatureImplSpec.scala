package com.featurebee

import org.scalatest.FeatureSpec
import FeatureDescription.State._
import ClientInfo.Browser._
import org.scalatest.OptionValues._

/**
 * @author Chris Wewerka
 */
class FeatureImplSpec extends FeatureSpec {
   
  val emptyClientInfo = ClientInfoImpl()
  
  feature("Standard feature activation") {
    scenario("Feature is active when using always on condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Development, Set(AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === true)
    }

    scenario("Feature is not active when using always off condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Development, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }

    scenario("Feature is not active when not all conditions are met") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Development, Set(AlwaysOffCondition, AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }
  }

  feature("God mode overriding of feature settings") {
    scenario("Overriding has precedence") {
      val clientInfoForcedAlwaysOn = ClientInfoImpl(forcedFeatureToogle = (_) => Some(true))
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Development, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(clientInfoForcedAlwaysOn) === true)
    }
  }

  feature("Using block convenience methods in Feature for chrome only feature")  {
    val featureDescriptionChromeOnly = FeatureDescription("name", "desc", tags = Set(), Development, Set(BrowserCondition(Set(Chrome))))
    val feature = new FeatureImpl(featureDescriptionChromeOnly)

    scenario("Feature block for chrome clients is executed") {
      implicit val clientInfo = ClientInfoImpl(Some(Chrome))
      var evaluated = false

      val result = feature.ifActive {
        evaluated = true
        "feature block has been run"
      }

      assert(result.value === "feature block has been run")
      assert(evaluated === true)
    }

    scenario("Feature block for non chrome clients is not executed") {
      implicit val clientInfo = ClientInfoImpl(Some(Firefox))
      var evaluated = false

      val result = feature.ifActive {
        evaluated = true
        "feature block has been run"
      }

      assert(result.isEmpty)
      assert(evaluated === false)
    }

  }
}