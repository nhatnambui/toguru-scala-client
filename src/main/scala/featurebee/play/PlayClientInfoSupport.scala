package featurebee.play

import java.util.{UUID, Locale}

import play.api.http.HeaderNames
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import featurebee.FeaturesString._
import featurebee.api.Feature._

import scala.util.Try

/**
 * Default support methods for converting a [[RequestHeader]] to a [[featurebee.ClientInfo]].
 *
 * If your fine with the way this object extracts the locale, UUID, and forced feature activation string from the play
 * request header you simply have to write s.th like this in your app, and import it via import PlayControllerSupport._ in
 * your Controller classes.
 *
 * {{{
 *   object PlayControllerSupport {

    implicit def requestToClientInfo(implicit requestHeader: RequestHeader): ClientInfo = {
      import PlayClientInfoSupport._
      ClientInfoImpl(userAgent, localeFromCookieValue("culture"), uuidFromCookieValue("as24Visitor"), forcedFeatureToggle)
    }
  }
 * }}}
 */
object PlayClientInfoSupport {

  def userAgent(implicit requestHeader: RequestHeader) = requestHeader.headers.get(HeaderNames.USER_AGENT)

  def localeFromCookieValue(cookieName: String)(implicit requestHeader: RequestHeader): Option[Locale] = for {
    cultureCookie <- requestHeader.cookies.get(cookieName)
    lang <- Lang.get(cultureCookie.value)
  } yield lang.toLocale

  def uuidFromCookieValue(cookieName: String)(implicit requestHeader: RequestHeader): Option[UUID] =
    requestHeader.cookies.get(cookieName).flatMap(c => Try(UUID.fromString(c.value)).toOption)

  def forcedFeatureToggle(featureName: String)(implicit requestHeader: RequestHeader): Option[Boolean] = {

    def lowerCaseKeys[T](m: Map[String,T]) = m.map { case (k, v) => (k.toLowerCase, v) }

    val reqHeaders = lowerCaseKeys(requestHeader.headers.toSimpleMap)
    lazy val maybeForcedFromHeader = reqHeaders.get("x-featurebee").orElse(reqHeaders.get("featurebee")).
      flatMap(featuresString => parseForcedFeaturesString(featuresString)(featureName))
    lazy val maybeForcedFromCookie = requestHeader.cookies.get("featurebee").flatMap(cookie => parseForcedFeaturesString(cookie.value)(featureName))

    lazy val lowerCasedKeysQueryStringMap = lowerCaseKeys(requestHeader.queryString)

    lazy val parseFeatureQueryString: FeatureName => Option[Boolean] = {
      val maybeFeatureBeeString: Option[List[String]] = lowerCasedKeysQueryStringMap.get("featurebee").map(_.toList)

      featureName => {
        maybeFeatureBeeString.map {
          case Nil => None
          case featureString :: Nil =>
            parseForcedFeaturesString(featureString)(featureName)
          case other => throw new Exception(s"featurebee string has more than one element: $other ${other.size}")
        }.getOrElse(None)
      }
    }

    val maybeForcedFromQueryParam = parseFeatureQueryString(featureName)
    maybeForcedFromQueryParam.orElse(maybeForcedFromHeader).orElse(maybeForcedFromCookie)
  }
}

