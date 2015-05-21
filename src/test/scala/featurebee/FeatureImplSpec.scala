package featurebee

import featurebee.ClientInfo.Browser._
import featurebee.api.FeatureImpl
import featurebee.impl.FeatureDescriptionSingleton.State._
import featurebee.impl.{AlwaysOffCondition, AlwaysOnCondition, BrowserCondition, FeatureDescription}
import org.scalatest.FeatureSpec
import org.scalatest.OptionValues._

class FeatureImplSpec extends FeatureSpec {
   
  val emptyClientInfo = ClientInfoImpl()
  
  feature("Standard feature activation") {
    scenario("Feature is active when state is experimental and always on condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Experimental, Set(AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === true)
    }

    scenario("Feature is not active when state is experimental and always off condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Experimental, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }

    scenario("Feature is not active when state is experimental and not all conditions are met") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Experimental, Set(AlwaysOffCondition, AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }

    scenario("Feature is active when state is released") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), Released, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === true)
    }

    scenario("Feature is not active when state is in progress") {
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), InProgress, Set(AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }
  }

  feature("God mode overriding of feature settings") {
    scenario("Overriding has precedence") {
      val clientInfoForcedAlwaysOn = ClientInfoImpl(forcedFeatureToogle = (_) => Some(true))
      val featureDescription = FeatureDescription("name", "desc", tags = Set(), InProgress, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(clientInfoForcedAlwaysOn) === true)
    }
  }

  feature("Using block convenience methods in Feature for chrome only feature")  {
    val featureDescriptionChromeOnly = FeatureDescription("name", "desc", tags = Set(), Experimental, Set(BrowserCondition(Set(Chrome))))
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