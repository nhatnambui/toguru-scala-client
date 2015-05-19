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

object ClientInfo {

  object Browser extends Enumeration {
    type Browser = Value
    val Chrome, Ie, Firefox, Safari, Other = Value
  }
}
