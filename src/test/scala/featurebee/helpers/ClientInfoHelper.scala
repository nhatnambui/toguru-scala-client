package featurebee.helpers

import featurebee.api.Feature

/**
  * Created by matlloyd on 06/05/2016.
  */
object ClientInfoHelper {

  def forceFeatureTo(featureName: String, enabled: Boolean): (Feature.FeatureName => Option[Boolean]) = {
    (name: String) =>
      if(name == featureName) {
        Some(enabled)
      } else {
        None
      }
  }

}
