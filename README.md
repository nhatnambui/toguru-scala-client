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

Hint: If you want a dynamic S3 based json file registry, see farther below.

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

*IMPORTANT*: Please be aware that GodMode will work even if that Feature is not defined in the Registry or the Registry fails to load.
This means that `AlwaysOnFeature`/`AlwaysOffFeature` objects will be overridden by the GodMode.

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
  This defines a default ON or OFF activation of the feature for all clients. This state/activation may be overwritten by using god mode/forced feature toggling
  *IMPORTANT*: Please be aware that god mode only works if the feature is defined in the registry, i.e. the json in classpath or S3 contains the given feature. If 
  the feature is NOT present, and you define your features like above ```override def languageDropdown = Feature("language-dropdown").getOrElse(AlwaysOffFeature)```
  then the state defined by getOrElse(STATE) wins and forcing the feature to a specific state will not work. So this is a big difference between defining the
  default state of the feature in code with getOrElse and the default condition in the JSON!
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
  Be aware that in the standard impl of PlayClientInfoSupport this is derived by looking at a cookie value which is expected to be a UUID. If this cookie
  is not present, a random UUID is generated which means that the feature using trafficDistribution will not be stable for this client!
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

## Reloadable Feature Registry based on files in S3

Featurebee supports dynamic, periodic re-loading of the feature registry from a S3 bucket possibly containing several feature json files in the format as described
 above. For that to work you it's best to move the creation of the FeatureRegistry to the play guice context. 
 
S3 loading is supplied by S3JsonFeatureRegistry and the reloading feature is implemented by ReloadingFeatureRegistry.

### ReloadingFeatureRegistry
ReloadingFeatureRegistry inspects the last modification date of the inital and the re-creator function, adds the activationDelay duration to it and activates 
the registry on the resulting point in time. With that approach it should be possible to achieve that all instances of a service activate a new feature registry
at the same time and by that minimizing the problems experienced by end users. Would the instances switch the registry at different points in time some problems 
in their experience could arise.

See below for an example to enable periodic reloading of feature json files from S3.
 

```scala
import java.time.LocalDateTime
import java.util.concurrent.Executors
import akka.actor.ActorSystem
import com.amazonaws.services.s3.AmazonS3Client
import com.autoscout24.classifiedlist.TypedEvents.{FeatureRegistryLoadedFromS3WithIgnoredErrors, FeatureRegistryLoadingFromS3Failed, FeatureRegistrySuccessfullyLoadedFromS3}
import com.autoscout24.eventpublisher24.events._
import com.google.inject.{AbstractModule, Provides, Singleton}
import featurebee.api.FeatureRegistry
import featurebee.registry.DefaultFeatureValueFeatureRegistry
import featurebee.registry.s3.S3JsonFeatureRegistry.S3File
import featurebee.registry.s3.{ReloadingFeatureRegistry, S3JsonFeatureRegistry}
import org.scalactic.{Bad, Good}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class FeatureRegistryModule extends AbstractModule {

  private val bucketName = "as24prod-features-eu-west-1"

  def configure() = {}

  @Provides
  @Singleton
  def s3Client(configuration: Configuration): AmazonS3Client = new AmazonS3Client()

  @Provides
  @Singleton
  def featureRegistry(amazonS3Client: AmazonS3Client, actorSystem: ActorSystem, eventPublisher: TypedEventPublisher): FeatureRegistry = {

    val initialRegistry = s3FeatureRegistry(amazonS3Client, eventPublisher) match {
      case Some((registry, lastModified)) => (registry, lastModified)
      case None => (DefaultFeatureValueFeatureRegistry, LocalDateTime.MIN)
    }

    new ReloadingFeatureRegistry(initialRegistry, () => s3FeatureRegistry(amazonS3Client, eventPublisher),
      actorSystem.scheduler, reloadAfter = 2 minutes, activationDelay = 2 min 10 seconds, singleThreadExecContext
    )
  }

  // S3 Feature registry returns a merge of all feature json files and the latest modification date of all of them
  // in case of failures it returns None
  private val s3FeatureRegistry: (AmazonS3Client, TypedEventPublisher) => Option[(FeatureRegistry, LocalDateTime)] = {
    (amazonS3Client, eventPublisher) =>
      S3JsonFeatureRegistry(
        Seq(
          S3File(bucketName, "classified-list-featurebee.json", ignoreOnFailures = false),
          S3File(bucketName, "classified-list-gecloud-featurebee.json", ignoreOnFailures = true)
        )
      )(amazonS3Client) match {
        case Good(featureRegistryBuilt) =>
          val errorString = featureRegistryBuilt.failedIgnoredFiles.map(_.toString)
          if (errorString.nonEmpty) eventPublisher.publish(FeatureRegistryLoadedFromS3WithIgnoredErrors(errorString))
          else eventPublisher.publish(FeatureRegistrySuccessfullyLoadedFromS3())
          Some((featureRegistryBuilt.featureRegistry, featureRegistryBuilt.lastModified))

        case Bad(errors) =>
          eventPublisher.publish(FeatureRegistryLoadingFromS3Failed(errors.mkString("The following errors occured loading the features from S3", ";", "")))
          None
      }
  }

  private val singleThreadExecContext = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(1)
    def execute(runnable: Runnable) { threadPool.submit(runnable) }
    def reportFailure(t: Throwable) {}
  }
}
```
Please note that you are able to define files that don't break the reloading in case of errors (ignoreOnFailures). With that you are able to separate features for downstream
fragments (see below) and you're own features in two files with possibly different access policies and/or failure handling (ignore or not, as in above example)

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
