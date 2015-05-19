package com.featurebee

/**
 * @author Chris Wewerka
 */
trait Feature {
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
}

class FeatureImpl(desc: FeatureDescription) extends Feature {
  /**
   * Executes the block if the feature is active
   * @return a some if the block has been executed, containing the result of the block
   * @tparam T the return type of the block
   */
  override def ifActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (isActive) Some(block) else None

  /**
   * Executes the block if the feature is not active
   * @return a some if the block has been executed, containing the result of the block
   * @tparam T the return type of the block
   */
  override def ifNotActive[T](block: => T)(implicit clientInfo: ClientInfo): Option[T] = if (! isActive) Some(block) else None

  override def isActive(implicit clientInfo: ClientInfo): Boolean = {
    // TODO take state into account
    clientInfo.forcedFeatureToogle(desc.name).getOrElse {
      desc.activationConditions.forall(cond => cond.applies(clientInfo))
    }
  }
}

object Feature {
  type FeatureName = String
}
