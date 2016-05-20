package featurebee.registry.s3

import java.util.concurrent.atomic.AtomicReference

import akka.actor.Scheduler
import featurebee.api.Feature.FeatureName
import featurebee.api.{Feature, FeatureRegistry}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Feature registry that periodically reloads the feature registry via the given reCreator function. If the recreator function returns None, the
  * feature registry stays the same. With that you can implement keeping the old registry in case of failures or if you are able to detect that nothing
  * in the feature set has changed.
  */
class ReloadingFeatureRegistry(initial: FeatureRegistry, reCreator: () => Option[FeatureRegistry], scheduler: Scheduler,
                               reloadAfter: FiniteDuration = 3 minute, executor: ExecutionContext) extends FeatureRegistry {

  private val ref = new AtomicReference[FeatureRegistry](initial)
  scheduler.schedule(reloadAfter, reloadAfter) {
    reCreator().foreach(ref.set)
  }(executor)

  override def feature(name: String): Option[Feature] = ref.get().feature(name)

  override def allFeatures: Set[(FeatureName, Feature)] = ref.get().allFeatures
}
