package featurebee.impl

import featurebee.impl.FeatureDescriptionSingleton.State.StateType
import featurebee.impl.FeatureDescriptionSingleton.Tag

/**
 * @author Chris Wewerka
 */
case class FeatureDescription(name: String, description: String, tags: Set[Tag], state: StateType, conditions: Set[Condition])

object FeatureDescriptionSingleton {

  type Tag = String

  object State extends Enumeration {
    type StateType = Value
    val InDevelopment, UnderReview, Released = Value
  }
}

case class FeatureConfig(featureMetas: List[FeatureDescription])
