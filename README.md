# FeatureBee for Scala
FeatureBee client for Scala applications

## Status
[![Build Status](https://travis-ci.org/AutoScout24/featurebee-scala.svg)](https://travis-ci.org/AutoScout24/featurebee-scala)
[![Coverage Status](https://coveralls.io/repos/AutoScout24/featurebee-scala/badge.svg)](https://coveralls.io/r/AutoScout24/featurebee-scala)

## How to use it in your App
1. Write a Features trait to access all your features in one place, e.g.

    trait Features {
      def languageDropdown: Feature
      def survey: Feature
    }
    object Features extends Features {
      private implicit lazy val featureRegistry = StaticJsonFeatureRegistry("featureBee.json")
      override def languageDropdown = Feature("TATSU-232-Language-dropdown").getOrElse(AlwaysOffFeature)
      override def survey = Feature("TATSU-243-survey").getOrElse(AlwaysOffFeature)
    }
2. Write a support object which defines how the request from the client is used to extract relevant feature toggle info, 
like e.g. the language or the browser. For Play apps you may use the already defined PlayClientInfoSupport:

    object ControllerClientInfoSupport {
      implicit def requestToClientInfo(implicit requestHeader: RequestHeader): ClientInfo = {
        import PlayClientInfoSupport._
        ClientInfoImpl(userAgent, localeFromCookieValue("culture"), uuidFromCookieValue("as24Visitor"), forcedFeatureToggle)
      }
    }  
3. Currently only a static json file inside your deployment is supported, see Contract section below. See the usage of StaticJsonFeatureRegistry
above for infos how you specify the location of the feature config file.

## Forced feature toggling using the default PlayClientInfoSupport
If you use the PlayClientInfoSupport, you may force feature activation regardless of the conditions you specify in your
json feature config by setting a query param, a request Header, or a cookie. This order of mentioning the variants is also
the order of precedence, so query param has precedence over cookie. All the keys are case insensitive.

### Query param
Use query param 'featurebee' to specify forced/god mode activation of features and:
    http://yourUrl?featurebee=feature1%3Dtrue%7Cfeature2%3Dfalse
'=' is used to assign the true/false value to a feature with the given name and '|' is used to separate the different features 
from each other. So we need URL encoding here, so the above forced feature string would decode to
    feature1=true|feature2=false
    
### Request Header 
Use header name 'featurebee' or 'X-Featurebee' (case insensitiv) to specify the forced/god mode feature activation:
Example request header value:

    feature1=true|feature2=false
    
### Cookie
Use a cookie with name 'featurebee' to specify the forced/god mode feature activation:
Example cookie value:
 
    feature1=true|feature2=false
    
### Contract
The FeatureBee Server returns a list of all defined features.

    [{
      "name": "Name of the Feature",
      "description": "Some additional description",
      "tags": ["Team Name", "Or Service name"],
      "activation": [{"culture": ["de-DE"]}]
    }]

Conditions is an array of type and values. Supported types are "default", "culture", "userAgentFragments" and "trafficDistribution". 
Each condition can have its own format how the values should look like. Each condition could have multiple values. 
All conditions have to be fulfilled (logical AND). 

The json has to fulfill the following requirements:
* Name must be set and unique
* activation must contain at least one condition
* description is mandatory
* tags are optional
