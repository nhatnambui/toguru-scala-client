package toguru.play

import java.util.{UUID, Locale}

import play.api.http.HeaderNames
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import toguru.FeaturesString._
import toguru.api.Feature._

import scala.util.Try

/**
 * Default support methods for converting a RequestHeader to a ClientInfo.
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
    lazy val maybeForcedFromHeader = reqHeaders.get("x-toguru").orElse(reqHeaders.get("toguru")).
      flatMap(featuresString => parseForcedFeaturesString(featuresString)(featureName))
    lazy val maybeForcedFromCookie = requestHeader.cookies.get("toguru")
      .orElse(requestHeader.cookies.get("toguru"))
      .flatMap(cookie => parseForcedFeaturesString(cookie.value)(featureName))

    lazy val lowerCasedKeysQueryStringMap = lowerCaseKeys(requestHeader.queryString)

    lazy val parseFeatureQueryString: FeatureName => Option[Boolean] = {
      val maybeFeatureBeeString: Option[List[String]] = lowerCasedKeysQueryStringMap.get("toguru").map(_.toList)

      featureName => {
        maybeFeatureBeeString.map {
          case Nil => None
          case featureString :: _ => // we ignore double specified features by the client
            parseForcedFeaturesString(featureString)(featureName)
        }.getOrElse(None)
      }
    }

    val maybeForcedFromQueryParam = parseFeatureQueryString(featureName)
    maybeForcedFromQueryParam.orElse(maybeForcedFromHeader).orElse(maybeForcedFromCookie)
  }
}

