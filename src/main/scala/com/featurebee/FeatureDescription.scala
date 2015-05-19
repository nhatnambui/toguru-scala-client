package com.featurebee

import com.featurebee.FeatureDescription.State.State
import com.featurebee.FeatureDescription.Tag

/**
 * @author Chris Wewerka
 */
case class FeatureDescription(name: String, description: String, tags: Set[Tag], state: State, activationConditions: Set[Condition])

object FeatureDescription {

  type Tag = String

  object State extends Enumeration {
    type State = Value
    val Development, Review, Released = Value
  }
}

case class FeatureConfig(featureMetas: List[FeatureDescription])
