package featurebee.api

import featurebee.api.Feature.FeatureName

trait FeatureRegistry {
  def feature(name: String): Option[Feature]
  def allFeatures: Set[(FeatureName, Feature)]
}
