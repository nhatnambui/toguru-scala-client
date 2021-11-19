package toguru.impl

import toguru.api.Toggle.ToggleId
import toguru.api.{Activations, Condition, Toggle}

final case class ToggleStates(sequenceNo: Option[Long], toggles: Seq[ToggleState])

final case class ToggleState(id: String, tags: Map[String, String], condition: Condition) {
  lazy val servicesTag: Set[String] =
    tags.get("services").toVector.flatMap(_.split(",")).map(_.trim).toSet
  lazy val serviceTag: Option[String] =
    tags.get("service").map(_.trim)
}

object ToggleState {

  def apply(id: String, tags: Map[String, String], activations: Seq[ToggleActivation]): ToggleState = {

    val condition: Condition = {
      val rollout = for {
        activation <- activations.headOption
        rollout    <- activation.rollout
      } yield Condition.UuidRange(1 to rollout.percentage)

      val attributes = for {
        activation <- activations.headOption.toSeq
        (k, v)     <- activation.attributes
      } yield Attribute(k, v)

      (attributes :+ rollout.getOrElse(Condition.Off)).toList match {
        case Seq(c) => c
        case cs     => All(cs.toSet)
      }
    }

    new ToggleState(id, tags, condition)
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
