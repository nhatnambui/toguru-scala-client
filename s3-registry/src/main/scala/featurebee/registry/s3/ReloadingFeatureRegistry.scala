package featurebee.registry.s3

import java.time.{LocalDateTime, Duration => JavaDuration}
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
  *
  * @param initial the initial feature registry to be used together with it's last modification date. Use LocalDateTime.now if you cannot supply it.
  * @param reCreator function that possibly provides a new version of the feature registry together with it's last modification date, use LocalDateTime.now
  *                  if you cannot supply a value for that. If you experience errors on re-creation return None.
  * @param scheduler the scheduler for scheduling the registry re-creation
  * @param reloadAfter duration for scheduling feature reloading
  * @param activationDelay this duration is added to last modified date and the registry is activated on the resulting time. Using an activationDelay a
  *                        little bit higher than the reloadAfter time, all instances get a chance to reload and activate the new feature registry at the same
  *                        time
  * @param executor the execution context to provide a thread for reloading the registry
  */
class ReloadingFeatureRegistry(initial: (FeatureRegistry, LocalDateTime), reCreator: () => Option[(FeatureRegistry, LocalDateTime)], scheduler: Scheduler,
                               reloadAfter: FiniteDuration = 2 minute, activationDelay: FiniteDuration = 2 min 10 seconds, executor: ExecutionContext) extends FeatureRegistry {

  private val current = new AtomicReference[(FeatureRegistry, LocalDateTime)](initial)
  private val next = new AtomicReference[Option[(FeatureRegistry, LocalDateTime)]](None)

  scheduler.schedule(reloadAfter, reloadAfter) {
    reCreator().foreach {
      case (registry, newLastModified) =>
        val newActivationTime = newLastModified.plus(JavaDuration.ofNanos(activationDelay.toNanos))
        current.get() match {
          case (r, currentActTime) if newActivationTime.isAfter(currentActTime) => next.set(Some((registry, newActivationTime)))
          case _ =>
        }
    }
  }(executor)

  override def feature(name: String): Option[Feature] = registryToUse.feature(name)

  override def allFeatures: Set[(FeatureName, Feature)] = registryToUse.allFeatures

  private def registryToUse: FeatureRegistry = next.get().map {
    case (nextReg, activationTime) if LocalDateTime.now().isAfter(activationTime) =>
      current.set((nextReg, activationTime))
      next.set(None)
      nextReg
    case _ => current.get()._1
  }.getOrElse(current.get()._1)
}
