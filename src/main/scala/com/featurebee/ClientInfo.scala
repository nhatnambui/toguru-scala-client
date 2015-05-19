package com.featurebee

import java.util.Locale

import com.featurebee.ClientInfo.Browser.Browser
import com.featurebee.Feature.FeatureName

/**
 * @author Chris Wewerka
 */
trait ClientInfo {

  def browser: Option[Browser]
  def culture: Option[Locale]
  val forcedFeatureToogle: FeatureName => Option[Boolean]
}

case class ClientInfoImpl(browser: Option[Browser], culture: Option[Locale],
                          forcedFeatureToogle: FeatureName => Option[Boolean]) extends ClientInfo

object ClientInfo {

  object Browser extends Enumeration {
    type Browser = Value
    val Chrome, Ie, Firefox, Safari, Other = Value
  }
}
