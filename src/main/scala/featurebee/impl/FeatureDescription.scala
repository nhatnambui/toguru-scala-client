package featurebee.impl

import featurebee.impl.FeatureDescriptionSingleton.Tag

case class FeatureDescription(name: String, description: String, tags: Set[Tag], activation: Set[Condition])

object FeatureDescriptionSingleton {

  type Tag = String
}

case class FeatureConfig(featureMetas: List[FeatureDescription])
