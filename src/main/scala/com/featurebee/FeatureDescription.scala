package com.featurebee

import com.featurebee.FeatureDescription.State.State
import com.featurebee.FeatureDescription.Tag

/**
 * @author Chris Wewerka
 */
trait FeatureDescription {
  def name: String
  def description: String
  def tags: Set[Tag]
  def state: State
  def activationConditions: Set[Condition]
}

object FeatureDescription {

  type Tag = String

  object State extends Enumeration {
    type State = Value
    val Development, Review, Released = Value
  }
}

case class FeatureConfig(featureMetas: List[FeatureDescription])
