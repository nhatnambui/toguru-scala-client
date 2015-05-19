package featurebee

import java.util.Locale
import featurebee.ClientInfo.Browser.Browser

/**
 * @author Chris Wewerka
 */
sealed trait Condition {
  def applies(clientInfo: ClientInfo): Boolean
}

case object AlwaysOnCondition extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = true
}

case object AlwaysOffCondition extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = false
}

case class BrowserCondition(browsers: Set[Browser]) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = clientInfo.browser.exists(browsers.contains)
}

case class CultureCondition(cultures: Set[Locale]) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = clientInfo.culture.exists(cultures.contains)
}

case class TrafficDistribution(percentage: Double) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = ???
}
