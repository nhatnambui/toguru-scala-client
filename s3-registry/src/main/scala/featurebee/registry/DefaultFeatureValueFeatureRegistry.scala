package featurebee.registry

import featurebee.api.Feature.FeatureName
import featurebee.api.{Feature, FeatureRegistry}

/**
  * Always returns None for a requested feature. This is useful to always default to the hardcoded value of the feature as a fallback for registries that
  * may reload from remotes locations
  */
object DefaultFeatureValueFeatureRegistry extends FeatureRegistry {
  override def feature(name: String): Option[Feature] = None

  override def allFeatures: Set[(FeatureName, Feature)] = Set.empty
}
