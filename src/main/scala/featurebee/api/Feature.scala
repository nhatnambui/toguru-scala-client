package featurebee.api

import featurebee.ClientInfo
import featurebee.impl.{AlwaysOffCondition, AlwaysOnCondition, FeatureDescriptionHelper, FeatureDescription}

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
    * An optional description of the feature.
    */
  def featureDescription: Option[FeatureDescription]
}

abstract class BaseFeature extends Feature {
  override def ifActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (isActive) Some(block) else None
  override def ifNotActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (! isActive) Some(block) else None
}

/**
 * Use this object if you want to default to true for unknown feature names.
 */
object AlwaysOnFeature extends BaseFeature {
  val featureDescription = None
  override def isActive(implicit clientInfo: ClientInfo): Boolean = true
}

/**
 * Use this object if you want to default to false for unknown feature names.
 */
object AlwaysOffFeature extends BaseFeature {

  val featureDescription = None
  override def isActive(implicit clientInfo: ClientInfo): Boolean = false
}

class FeatureImpl(val desc: FeatureDescription) extends BaseFeature {

  val featureDescription = Some(desc)
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
