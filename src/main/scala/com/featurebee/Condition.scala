package com.featurebee

import java.util.Locale

import com.featurebee.ClientInfo.Browser.Browser

/**
 * @author Chris Wewerka
 */
sealed trait Condition {
  def applies(clientInfo: ClientInfo): Boolean
}

case class BrowserCondition(browser: Browser) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = clientInfo.browser.contains(browser)
}

case class CultureCondition(culture: Locale) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = clientInfo.culture.contains(culture)
}

case class TrafficDistribution(percentage: Double) extends Condition {
  override def applies(clientInfo: ClientInfo): Boolean = ???
}
