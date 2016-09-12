package toguru.helpers

import toguru.api.Feature

/**
  * Created by matlloyd on 06/05/2016.
  */
object ClientInfoHelper {

  /**
    * Helper function to force a feature to a particular state
    * @param featureName The identifier name for the feature
    * @param enabled Should this feature be forced to be enabled or disabled?
    * @return A function that forces `featureName` to be a particular state
    */
  def forceFeatureTo(featureName: String, enabled: Boolean): (Feature.FeatureName => Option[Boolean]) = {
    (name: String) =>
      if(name == featureName) {
        Some(enabled)
      } else {
        None
      }
  }

}
