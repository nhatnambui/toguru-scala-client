package featurebee.registry

import featurebee.api.Feature.FeatureName
import featurebee.api.{AlwaysOnFeature, Feature, FeatureRegistry}

object AlwaysOnFeatureRegistry extends FeatureRegistry {
  override def feature(name: String): Option[Feature] = Some(AlwaysOnFeature(name))
  override def allFeatures: Set[(FeatureName, Feature)] = Set()
}
