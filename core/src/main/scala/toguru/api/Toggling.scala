package toguru.api

import toguru.impl.{ToggleState, TogglesString}

/**
  * Contains the information needed by [[toguru.api.Toggle]]s to make toggling decisions.
  */
trait Toggling {

  def client: ClientInfo

  def activations: Activations

  /**
    * Returns the activation state of the given toggle.
    *
    * @param toggle the toggle in question
    * @return
    */
  def apply(toggle: Toggle): Boolean =
    client.forcedToggle(toggle.id).getOrElse(activations(toggle).applies(client))

  /**
    * Returns all activations.
    *
    * @return
    */
  def apply(): Iterable[ToggleState] = activations()

  /**
    * Returns a toggling string for downstream services.
    *
    * @param service the name of the downstream service - must match the "service" tag or be included in the "services" tag defined on the toggle on
    *                the toguru server.
    * @return
    */
  def toggleStringForService(service: String): String = {
    val toggleStates =
      activations
        .togglesFor(service)
        .map {
          case (toggleId, condition) =>
            toggleId -> client.forcedToggle(toggleId).getOrElse(condition.applies(client))
        }

    TogglesString.build(toggleStates)
  }

  /**
    * Returns a toggling string that can be sent upstream that considers:
    *  - Any toggle that was forced by the client
    *  - Any toggle that is tagged with the services parameter and it is either not rolled out at 0 or 100%,
    *    or locally defined as always on.
    *
    * @param services A collection of service names to be evaluated against the service or services tags of the toggle.
    *                 Typically, if a service B is interested in receiving toggle X from service A, toggle X should add
    *                 service A to its `services` tag (i.e the `services` tag should contain the name of the services
    *                 that should forward the toggle).
    * @return a string that can be added to a toguru querystring or header
    */
  def buildForwardingToggleString(services: Set[String]): String =
    TogglesString.build(
      activations
        .apply()
        .map(toggleState =>
          (
            toggleState.id,
            client.forcedToggle(toggleState.id),
            services.exists(toggleState.serviceTagsContains),
            toggleState.rolloutPercentage,
            toggleState.condition.applies(client)
          )
        )
        .collect {
          case (id, Some(forcedToggleValue), _, _, _)                                           => id -> forcedToggleValue
          case (id, _, true, Some(percentage), condition) if percentage > 0 && percentage < 100 => id -> condition
          case (id, _, true, None, true)                                                        => id -> true

        }
    )
}

/**
  * Simple case class-based implementation of the Toggling trait. See also [[toguru.api.ToguruClient#apply]]
  *
  * @param client the client information to use
  * @param activations the activation conditions to use
  */
final case class TogglingInfo(client: ClientInfo, activations: Activations) extends Toggling
