package featurebee

import java.util.Locale
import ClientInfo.Browser.Browser
import featurebee.api.Feature
import Feature.FeatureName

/**
 * @author Chris Wewerka
 */
trait ClientInfo {

  def browser: Option[Browser]
  def culture: Option[Locale]
  val forcedFeatureToogle: FeatureName => Option[Boolean]
}

case class ClientInfoImpl(browser: Option[Browser] = None, culture: Option[Locale] = None,
                          forcedFeatureToogle: FeatureName => Option[Boolean] = (_) => None) extends ClientInfo

object ClientInfo {

  object Browser extends Enumeration {
    type Browser = Value
    val Chrome, Ie, Firefox, Safari, Other = Value
  }
}
