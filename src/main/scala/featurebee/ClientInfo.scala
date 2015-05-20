package featurebee

import java.util.{UUID, Locale}
import ClientInfo.Browser.Browser
import featurebee.api.Feature
import Feature.FeatureName

trait ClientInfo {

  def browser: Option[Browser]
  def culture: Option[Locale]

  /** identifies the client/the request */
  def uuid: Option[UUID]

  val forcedFeatureToogle: FeatureName => Option[Boolean]
}

case class ClientInfoImpl(browser: Option[Browser] = None, culture: Option[Locale] = None, uuid: Option[UUID] = None,
                          forcedFeatureToogle: FeatureName => Option[Boolean] = (_) => None) extends ClientInfo

object ClientInfo {

  object Browser extends Enumeration {
    type Browser = Value
    val Chrome, Ie, Firefox, Safari, Other = Value
  }
}
