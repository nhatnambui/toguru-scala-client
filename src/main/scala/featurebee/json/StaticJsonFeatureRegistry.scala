package featurebee.json

import featurebee.{FeaturesString, ClientInfo}
import featurebee.api.Feature.FeatureName
import featurebee.api.{FeatureImpl, Feature, FeatureRegistry}
import FeatureJsonProtocol._
import featurebee.impl.{FeatureDescription}
import org.apache.commons.io.IOUtils
import spray.json.DefaultJsonProtocol
import spray.json._

@deprecated(message = "Use JsonFeatureRegistry", since = "20.05.2016")
class StaticJsonFeatureRegistry(json: String) extends JsonFeatureRegistry(json)

/**
  * Creates a feature registry based on the given json feature descriptions.
  * If multiple features with the same name are defined, earlier occurrences (in a file or multiple files) have preference.
  */
class JsonFeatureRegistry(jsons: Seq[String]) extends FeatureRegistry {

  def this(json: String) = this(Seq(json))

  private val featureDescriptions: Seq[FeatureDescription] = {
    val descriptions = jsons.flatMap(_.parseJson.convertTo[Seq[FeatureDescription]]).map(fd => fd.copy(name = fd.name.toLowerCase))
    val featureNames: Seq[String] = descriptions.map(_.name)

    featureNames.toSet.flatMap {
      name: String => descriptions.collectFirst {
        case fd@FeatureDescription(fdName, _, _, _, _) if fdName == name => fd
      }
    }.toSeq
  }


  private val features: Map[FeatureName, Feature] = featureDescriptions.
    map(desc => desc.name -> new FeatureImpl(desc)).toMap

  override def feature(name: String): Option[Feature] = features.get(name.toLowerCase)

  override def allFeatures: Set[(FeatureName, Feature)] = features.toSet
}

object StaticJsonFeatureRegistry {
  def apply(classpathRelativePath: String): FeatureRegistry = {
    val jsonInputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(classpathRelativePath)
    new JsonFeatureRegistry(IOUtils.toString(jsonInputStream))
  }
}
