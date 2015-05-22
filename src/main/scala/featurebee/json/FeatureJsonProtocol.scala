package featurebee.json

import java.util.Locale

import featurebee.impl._
import spray.json._

import scala.util.matching.Regex


object FeatureJsonProtocol extends DefaultJsonProtocol {

  implicit object ConditionJsonFormat extends RootJsonFormat[Condition] {

    def mapLocale(localeAsString: String): Locale = {
      localeAsString.split('-').toList match {
        case lang::country::Nil => new Locale(lang, country)
        case lang::Nil if lang.toLowerCase == lang => new Locale(lang)
        case country::Nil if country.toUpperCase == country => new Locale("", country)
        case _ => throw new DeserializationException(s"Invalid locale string in feature description: $localeAsString")
      }
    }

    def mapLocales(locales: Vector[JsValue]) = {
      val jLocales = locales.toList.map {
        case jsString: JsString => jsString.value
        case other => throw new DeserializationException(s"Culture should be a json string but is ${other.getClass}")
      }.map(mapLocale)

      CultureCondition(jLocales.toSet)
    }

    def mapUserAgentFragments(userAgentFrags: Vector[JsValue]) = {
      val userAgentsFragments = userAgentFrags.toList.map {
        case JsString(userAgentFrag) => userAgentFrag
        case other => throw new DeserializationException(s"Browser should be a json string but is ${other.getClass}")
      }
      UserAgentCondition(userAgentsFragments.toSet)
    }

    private val rangeRegex = """(\d{1,3})-(\d{1,3})""".r
    def parseToRange(s: String): Range = {
      s match {
        case rangeRegex(start, end) if start.toInt > 0 && end.toInt <= 100 && start.toInt <= end.toInt => start.toInt to end.toInt
        case other => throw new DeserializationException(s"Expected range (min=1, max=100) in format e.g. 5-10 but got $other")
      }
    }

    def mapUuidRanges(uuidRanges: Vector[JsValue]) = {
      val ranges = uuidRanges.toList.map {
        case jsString: JsString => parseToRange(jsString.value)
        case other => throw new DeserializationException(s"Uuid Distribution range should be a json string in the format e.g. '5-10' but is ${other.getClass}")
      }
      UuidDistributionCondition(ranges, UuidDistributionCondition.defaultUuidToIntProjection)
    }
    
    def write(c: Condition) = throw new DeserializationException("Write not supported for conditions")
    def read(value: JsValue) = {
      value.asJsObject.fields.get("default").collect { case JsBoolean(staticActivate) => if(staticActivate) AlwaysOnCondition else AlwaysOffCondition }.orElse {
        value.asJsObject.fields.get("culture").collect { case JsArray(locales) => mapLocales(locales) }.orElse {
          value.asJsObject.fields.get("userAgentFragments").collect { case JsArray(userAgentFragements) => mapUserAgentFragments(userAgentFragements) }.orElse {
            value.asJsObject.fields.get("trafficDistribution").collect {
              case JsArray(uuidRanges) => mapUuidRanges(uuidRanges)
              case jsSingleUuidRange => mapUuidRanges(Vector(jsSingleUuidRange))
            }
          }
        }
      }.getOrElse(throw new DeserializationException(s"Unsupported condition(s)/conditionFormat: ${value.asJsObject.fields.keys}"))
    }
  }

  implicit val featureDescriptionFormat = jsonFormat4(FeatureDescription)
}
