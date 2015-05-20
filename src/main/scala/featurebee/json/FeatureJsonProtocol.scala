package featurebee.json

import java.util.Locale

import featurebee.ClientInfo.Browser
import featurebee.impl._
import featurebee.impl.FeatureDescriptionSingleton.State.StateType
import featurebee.impl.FeatureDescriptionSingleton.State
import spray.json._
import DefaultJsonProtocol._


object FeatureJsonProtocol extends DefaultJsonProtocol {

  implicit object StateJsonFormat extends RootJsonFormat[StateType] {
    val stateEnums = State.values.toList
    override def write(obj: StateType): JsValue = throw new DeserializationException("Write not supported for state")
    override def read(json: JsValue): StateType = {
      stateEnums.find {
        st =>
          json match {
            case jsStringState: JsString =>
              val state = st.toString.toLowerCase
              state.toLowerCase == jsStringState.value.toLowerCase
            case _ => throw new DeserializationException(s"State has to be a json string: $json")
          }
      }.getOrElse(throw new DeserializationException(s"Unknown state ${json.toString()}"))
    }
  }

  implicit object ConditionJsonFormat extends RootJsonFormat[Condition] {

    val allBrowsers = Browser.values.toList
    
    def mapLocales(locales: Vector[JsValue]) = {
      val jLocales = locales.toList.map {
        case jsString: JsString =>
          val splitted = jsString.value.split('-')
          splitted.length match {
            case 2 => new Locale(splitted(0), splitted(1))
            case 1 => new Locale(splitted(0))
            case _ => throw new DeserializationException(s"Invalid locale string in feature description: ${jsString.value}")
          }
        case other => throw new DeserializationException(s"Culture should be a json string but is ${other.getClass}")
      }
      CultureCondition(jLocales.toSet)
    }

    def mapBrowsers(browsers: Vector[JsValue]) = {
      val brs = browsers.toList.map {
        case jsStringBrowser: JsString =>
          allBrowsers.find(br => br.toString.toLowerCase == jsStringBrowser.value.toLowerCase).getOrElse(Browser.Other)
        case other => throw new DeserializationException(s"Browser should be a json string but is ${other.getClass}")
      }
      BrowserCondition(brs.toSet)
    }

    def mapUuidRanges(uuidRanges: Vector[JsValue]) = {

      val rangeRegex = """(\d{1,2})-(\d{1,2})""".r

      def parseToRange(s: String): Range = {
          s match {
            case rangeRegex(start, end) if start.toInt > 0 && end.toInt <= 100 => start.toInt to end.toInt
            case other => throw new DeserializationException(s"Expected range (min=1, max=100) in format e.g. 5-10 but got $other")
          }
      }

      val ranges = uuidRanges.toList.map {
        case jsString: JsString => parseToRange(jsString.value)
        case other => throw new DeserializationException(s"Uuid Distribution range should be a json string in the format e.g. '5-10' but is ${other.getClass}")
      }
      UuidDistributionCondition(ranges, UuidDistributionCondition.defaultUuidToIntProjection)
    }
    
    def write(c: Condition) = throw new DeserializationException("Write not supported for conditions")
    def read(value: JsValue) = {
      value.asJsObject.fields.get("culture").collect { case JsArray(locales) => mapLocales(locales) }.orElse {
        value.asJsObject.fields.get("browser").collect { case JsArray(browsers) => mapBrowsers(browsers)}.orElse {
          value.asJsObject.fields.get("uuidDistribution").collect {
            case JsArray(uuidRanges) => mapUuidRanges(uuidRanges)
            case jsSingleUuidRange => mapUuidRanges(Vector(jsSingleUuidRange))
          }
        }
      }.getOrElse(throw new DeserializationException(s"Unsupported condition(s)/conditionFormat: ${value.asJsObject.fields.keys}"))
    }
  }

  implicit val featureDescriptionFormat = jsonFormat5(FeatureDescription)
}


sdasd