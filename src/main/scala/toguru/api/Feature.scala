package toguru.api

import toguru.ClientInfo
import toguru.api.Feature.FeatureName
import toguru.impl.{AlwaysOffCondition, AlwaysOnCondition, FeatureDescription, FeatureDescriptionHelper}

import scala.annotation.implicitNotFound

sealed trait Feature {
  /**
   * Executes the block if the feature is active
   * @return a some if the block has been executed, containing the result of the block
   * @tparam T the return type of the block
   */
  def ifActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T]

  /**
   * Executes the block if the feature is not active
   * @return a some if the block has been executed, containing the result of the block
   * @tparam T the return type of the block
   */
  def ifNotActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T]

  def isActive(implicit clientInfo: ClientInfo): Boolean

  /**
    * the metadata of the feature.
    */
  def featureDescription: FeatureDescription
}

abstract class BaseFeature extends Feature {
  override def ifActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (isActive) Some(block) else None
  override def ifNotActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (! isActive) Some(block) else None
}

/**
 * Use this object if you want to default to true for unknown feature names.
 */
case class AlwaysOnFeature(name: FeatureName) extends FeatureImpl(FeatureDescription(name, "a feature that is always on", None, Set(AlwaysOnCondition)))

/**
 * Use this object if you want to default to false for unknown feature names.
 */
case class AlwaysOffFeature(name: FeatureName) extends FeatureImpl(FeatureDescription(name, "a feature that is always off", None, Set(AlwaysOffCondition)))

class FeatureImpl(val desc: FeatureDescription) extends BaseFeature {

  val featureDescription = desc
  override def isActive(implicit clientInfo: ClientInfo): Boolean = {
    clientInfo.forcedFeatureToggle(desc.name).getOrElse {
      desc.activation.forall(cond => cond.applies(clientInfo))
    }
  }
}

object Feature {
  type FeatureName = String

  @implicitNotFound("Please import your desired implementation of FeatureRegistry")
  def apply(name: String)(implicit featureRegistry: FeatureRegistry): Option[Feature] = featureRegistry.feature(name)
}
