# FeatureBee For Scala

FeatureBee client for Scala applications

## Status

[![Build Status](https://travis-ci.org/AutoScout24/featurebee-scala.svg)](https://travis-ci.org/AutoScout24/featurebee-scala)
[![Coverage Status](https://coveralls.io/repos/AutoScout24/featurebee-scala/badge.svg)](https://coveralls.io/r/AutoScout24/featurebee-scala)

## Setup

Add to your `build.sbt` following resolver with dependency:

```scala
resolvers += Resolver.bintrayRepo("tatsu-kondor", "maven")

libraryDependencies += "com.autoscout24" %% "featurebee" % "1.0.97"
```

Now you can use FeatureBee library.

## How To Use

Write a Features trait to access all your features in one place, e.g.:

```scala
trait Features {
  def languageDropdown: Feature
  def survey: Feature
}

object Features extends Features {
  private implicit lazy val featureRegistry = StaticJsonFeatureRegistry("featureBee.json")
  override def languageDropdown = Feature("language-dropdown").getOrElse(AlwaysOffFeature)
  override def survey = Feature("survey").getOrElse(AlwaysOffFeature)
}
```

Add file `featureBee.json` at your `resources` or `conf` folder with `JSON` that explains behaviour of your feature flags, for example:

```json
[
  {
    "name": "survey",
    "description": "Enables survey",
    "tags": ["our-team", "awesome-feature"],
    "activation": [{ "default": true}]
  },
  {
    "name": "language-dropdown",
    "description": "Shows language dropdown",
    "tags": ["our-team", "awesome-feature"],
    "activation": [{ "default": false}]
  }
]
```

For deatil information regarding format of `JSON` see [Contract](#contract) paragraph.

Write a support object which defines how the request from the client is used to extract relevant feature toggle info, like e.g. the language or the browser. For Play apps you may use the already defined `PlayClientInfoSupport`:

```scala
object ControllerClientInfoSupport {
  implicit def requestToClientInfo(implicit requestHeader: RequestHeader): ClientInfo = {
    import PlayClientInfoSupport._
    ClientInfoImpl(userAgent, localeFromCookieValue("culture"), uuidFromCookieValue("as24Visitor"), forcedFeatureToggle)
  }
}  
```

Currently only a static json file inside your deployment is supported, see Contract section below. See the usage of `StaticJsonFeatureRegistry` above for infos how you specify the location of the feature config file.

## Forced Feature Toggling (GodMode)

If you use the `PlayClientInfoSupport`, you may force feature activation regardless of the conditions you specify in your `JSON` feature config by setting a query param, a request Header, or a cookie. This order of mentioning the variants is also the order of precedence, so query param has precedence over cookie. All the keys are case insensitive.

### Query Param

Use query param `featurebee` to specify forced / god mode activation of features:

```
http://yourUrl?featurebee=feature1%3Dtrue%7Cfeature2%3Dfalse 
```

Which decodes to:

```
http://yourUrl?featurebee=feature1=true|feature2=false)
```

`=` is used to assign the `true` / `false` value to a feature with the given name and `|` is used to separate the different features from each other. So we need URL encoding here, so the above forced feature string would decode to:

```
feature1=true|feature2=false
```

### Request Header 

Use header name `featurebee` or `X-Featurebee` (case insensitiv) to specify the forced / god mode feature activation. 

Example request header value:

```
feature1=true|feature2=false
```

### Cookie

Use a cookie with name `featurebee` to specify the forced/god mode feature activation.

Example cookie value:

``` 
feature1=true|feature2=false
```

## Contract

The FeatureBee Server returns a list of all defined features.

```json
[{
  "name": "Name of the Feature",
  "description": "Some additional description",
  "tags": ["Team Name", "Or Service name"],
  "activation": [{"culture": ["de-DE"]}]
}]
```

Conditions is an array of type and values. Supported types are `default`, `culture`, `userAgentFragments` and `trafficDistribution`. 

Each condition can have its own format how the values should look like. Each condition could have multiple values. 
All conditions have to be fulfilled (logical AND). 

### Format of conditions
* `default`: JSON Boolean (`true` or `false`) or JSON String (`"on"` or `"off"`)
  * Examples: 
    * `{ "default": true }` or `{ "default": false }` 
    * `{ "default": "on" }` or `{ "default": "off" }`
* `culture`: JSON Array of Strings in the form `"lang-COUNTRY"` or `"lang"` (only lower case) or `"COUNTRY"` (only upper case)
  * Examples: 
    * `{ "culture": ["de-AT"] }` 
    * `{ "culture": ["AT"] }` 
    * `{ "culture": ["de"] }` 
    * `{ "culture": ["de-DE", "de-AT] }` 
* `userAgentFragments`: JSON Array of Strings that must be contained in the user agent
  * Example: `{"userAgentFragments": ["Firefox"]}`
* `trafficDistribution`: JSON Array or a single JSON String in the from `"FROM-TO"` where `TO` > `FROM` and `1 <= FROM,TO <= 100`. 
  * Examples: 
    * `{ "trafficDistribution": "1-100" }` 
    * `{ "trafficDistribution": "51-100" }` 
    * `{ "trafficDistribution": "1-20" }` 
    * `{ "trafficDistribution": ["1-20", "80-85"] }`

The `JSON` has to fulfill the following requirements:
* Name must be set and unique
* activation must contain at least one condition
* description is mandatory
* tags are optional

## Fragment Services & Features

When working with Fragments you may need to pass the feature toggles through to Fragment Service.

To do this you can define the services your feature is required in. From there you can call the Feature Registry and 
get the Feature String for a particular service.

e.g:

```json
[{
  "name": "name-of-the-feature",
  "description": "Some additional description",
  "tags": ["Team Name", "Or Service name"],
  "activation": [{"default": true}],
  "services": ["content-service"]
},
{
  "name": "name-of-another-feature",
  "description": "Some other description",
  "tags": ["Team Name", "Or Service name"],
  "activation": [{"default": true}],
  "services": []
}]
```

Where you are including your fragment you can now do:

```scala
val featureString = featureRegistry.featureStringForService("content-service")
@Html(s"""<!--#include virtual="/fragment/contentservice/header.html?featurebee=${featureString}" -->""")
```

This will only pass across the appropriate features to the fragment service. e.g:

`/fragment/contentservice/header.html?featurebee=name-of-the-feature=true`

## Copyright

Copyright (C) 2015 AutoScout24 GmbH.

Distributed under the MIT License.
