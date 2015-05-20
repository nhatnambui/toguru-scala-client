package featurebee.json

import java.util.Locale

import featurebee.ClientInfo.Browser
import featurebee.impl.{BrowserCondition, CultureCondition, FeatureDescription, Condition}
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
    
    def write(c: Condition) = throw new DeserializationException("Write not supported for conditions")
    def read(value: JsValue) = {
      value.asJsObject.fields.get("culture").collect { case JsArray(locales) => mapLocales(locales) }.orElse {
        value.asJsObject.fields.get("browser").collect { case JsArray(browsers) => mapBrowsers(browsers)}
        // TODO traffic dist
      }.getOrElse(throw new DeserializationException(s"Unsupported condition(s): ${value.asJsObject.fields.keys}"))
    }
  }

  implicit val featureDescriptionFormat = jsonFormat5(FeatureDescription)
}
