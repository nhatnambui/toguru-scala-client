package featurebee.api

import featurebee.ClientInfo
import featurebee.api.Feature.FeatureName

trait FeatureRegistry {
  def feature(name: String): Option[Feature]
  def allFeatures: Set[(FeatureName, Feature)]
  def featureStringForService(service: String)(implicit clientInfo: ClientInfo): String
}
