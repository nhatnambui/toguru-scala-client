package featurebee.registry

import featurebee.api.Feature.FeatureName
import featurebee.api.{AlwaysOffFeature, AlwaysOnFeature, Feature, FeatureRegistry}

object AlwaysOffFeatureRegistry extends FeatureRegistry {
  override def feature(name: String): Option[Feature] = Some(AlwaysOffFeature(name))
  override def allFeatures: Set[(FeatureName, Feature)] = Set()
}
