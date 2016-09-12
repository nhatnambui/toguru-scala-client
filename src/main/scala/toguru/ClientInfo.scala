package toguru

import java.util.{UUID, Locale}
import toguru.ClientInfo.UserAgent
import toguru.api.Feature
import Feature.FeatureName

trait ClientInfo {

  def userAgent: Option[UserAgent]
  def culture: Option[Locale]

  /** identifies the client/the request */
  def uuid: Option[UUID]

  val forcedFeatureToggle: FeatureName => Option[Boolean]
}

case class ClientInfoImpl(userAgent: Option[UserAgent] = None, culture: Option[Locale] = None, uuid: Option[UUID] = None,
                          forcedFeatureToggle: FeatureName => Option[Boolean] = (_) => None) extends ClientInfo

object ClientInfo {
  type UserAgent = String
}