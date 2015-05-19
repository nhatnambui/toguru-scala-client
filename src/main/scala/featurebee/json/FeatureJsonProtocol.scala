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
    override def write(obj: StateType): JsValue = ???
    override def read(json: JsValue): StateType = {
      stateEnums.find{
        st => val state = st.toString.toLowerCase
          val jsonState: JsString = json.asInstanceOf[JsString]
          state.toLowerCase == jsonState.value.toLowerCase
      }.getOrElse(throw new DeserializationException(s"Unknown state ${json.toString()}"))
    }
  }

  implicit object ConditionJsonFormat extends RootJsonFormat[Condition] {
    
    def mapLocales(locales: Vector[JsValue]) = {
      val jLocales = locales.toList.map {
        e =>
          val splitted = e.asInstanceOf[JsString].value.split('-')
          new Locale(splitted(0), splitted(1))

      }
      CultureCondition(jLocales.toSet)
    }

    def mapBrowsers(browsers: Vector[JsValue]) = {
      val brs = browsers.toList.map {
        e =>
          val b = e.asInstanceOf[JsString].value
          val allBrowsers = Browser.values.toList
          allBrowsers.find(br => br.toString.toLowerCase == b.toLowerCase).getOrElse(Browser.Other)
      }
      BrowserCondition(brs.toSet)
    }
    
    def write(c: Condition) = ???
    def read(value: JsValue) = {
      value.asJsObject.fields.get("culture").map { case JsArray(locales) => mapLocales(locales) }.orElse {
        value.asJsObject.fields.get("browser").map { case JsArray(browsers) => mapBrowsers(browsers)}
        // TODO traffic dist
      }.getOrElse(throw new DeserializationException("Subclass of Condition expected"))
    }
  }

  implicit val featureDescriptionFormat = jsonFormat5(FeatureDescription)
}
