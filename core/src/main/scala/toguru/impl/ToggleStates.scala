package toguru.impl

import toguru.api.Toggle.ToggleId
import toguru.api.{Activations, Condition, Toggle}

final case class ToggleStates(sequenceNo: Option[Long], toggles: Seq[ToggleState])

final case class ToggleState(id: String, tags: Map[String, String], condition: Condition, rolloutPercentage: Option[Int]) {
  lazy val servicesTag: Set[String] =
    tags.get("services").toVector.flatMap(_.split(",")).map(_.trim).toSet
  lazy val serviceTag: Option[String] =
    tags.get("service").map(_.trim)
  def serviceTagsContains(serviceName: String): Boolean =
    servicesTag.contains(serviceName) || serviceTag.contains(serviceName)
}

object ToggleState {

  def apply(id: String, tags: Map[String, String], activations: Seq[ToggleActivation]): ToggleState = {

    val rolloutPercentage: Option[Int] = for {
      activation <- activations.headOption
      rollout    <- activation.rollout
    } yield rollout.percentage

    val rollout: Option[Condition] = rolloutPercentage.map(percentage => Condition.UuidRange(1 to percentage))

    val condition: Condition = {
      val attributes = for {
        activation <- activations.headOption.toSeq
        (k, v)     <- activation.attributes
      } yield Attribute(k, v)

      (attributes :+ rollout.getOrElse(Condition.Off)).toList match {
        case Seq(c) => c
        case cs     => All(cs.toSet)
      }
    }

    new ToggleState(id, tags, condition, rolloutPercentage)
  }

}

final case class ToggleActivation(rollout: Option[Rollout] = None, attributes: Map[String, Seq[String]] = Map.empty)

final case class Rollout(percentage: Int)

final class ToggleStateActivations(toggleStates: ToggleStates) extends Activations {

  private val conditions = toggleStates.toggles.map(ts => ts.id -> ts.condition).toMap

  override def apply(toggle: Toggle): Condition = conditions.getOrElse(toggle.id, toggle.default)

  override def apply(): Iterable[ToggleState] = toggleStates.toggles

  override def togglesFor(service: String): Map[ToggleId, Condition] =
    toggleStates.toggles
      .filter(toggle => toggle.serviceTag.exists(_ == service) || toggle.servicesTag.contains(service))
      .map(toggle => toggle.id -> conditions(toggle.id))
      .toMap

  override def stateSequenceNo: Option[Long] = toggleStates.sequenceNo
}
