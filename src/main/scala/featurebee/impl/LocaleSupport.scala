package featurebee.impl

import java.util.Locale

object LocaleSupport {

  implicit class RichLocale(locale: Locale) {

    def lang: Option[String] = locale.getLanguage match {
      case "" | null => None
      case lang => Some(lang) // always lowercase
    }

    def country: Option[String] = locale.getCountry match {
      case "" | null => None
      case country => Some(country) // always uppercase
    }
  }


}


