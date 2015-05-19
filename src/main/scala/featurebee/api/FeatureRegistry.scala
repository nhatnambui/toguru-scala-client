package featurebee.api

trait FeatureRegistry {
  def feature(name: String): Option[Feature]
  def allFeatures: Set[Feature]
}
