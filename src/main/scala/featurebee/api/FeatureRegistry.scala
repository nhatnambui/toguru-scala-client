package featurebee.api

import featurebee.{FeaturesString, ClientInfo}
import featurebee.api.Feature.FeatureName
import featurebee.impl.FeatureDescription

trait FeatureRegistry {
  def feature(name: String): Option[Feature]
  def allFeatures: Set[(FeatureName, Feature)]

  /**
    * Builds a feature string for a specific service.
    *
    * @param service    The service identifier to build the feature string for.
    * @param clientInfo The current client information
    * @return A feature string in the format of feature1=true|feature2=false|feature3=true
    */
  def featureStringForService(service: String)(implicit clientInfo: ClientInfo): String = {
    val serviceFeatures = allFeatures.collect {
      case (_, feature) if feature.featureDescription.services.exists(s => s.contains(service)) => feature
    }
    FeaturesString.buildFeaturesString(serviceFeatures)
  }
}
