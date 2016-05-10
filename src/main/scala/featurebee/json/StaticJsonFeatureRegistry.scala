package featurebee.json

import featurebee.{FeaturesString, ClientInfo}
import featurebee.api.Feature.FeatureName
import featurebee.api.{FeatureImpl, Feature, FeatureRegistry}
import FeatureJsonProtocol._
import featurebee.impl.{FeatureDescription}
import org.apache.commons.io.IOUtils
import spray.json.DefaultJsonProtocol
import spray.json._

class StaticJsonFeatureRegistry(json: String) extends FeatureRegistry {
  private val featureDescriptions: Seq[FeatureDescription] = json.parseJson.convertTo[Seq[FeatureDescription]]

  private val featureNames: Seq[String] = featureDescriptions.map(_.name.toLowerCase)
  if(featureNames.size != featureNames.toSet.size) throw new IllegalStateException("Feature json config may not contain duplicate feature names (it is also case insensitive)")

  private val features: Map[FeatureName, Feature] = featureDescriptions.
    map(desc => desc.name -> new FeatureImpl(desc)).toMap
  
  override def feature(name: String): Option[Feature] = features.get(name)
  override def allFeatures: Set[(FeatureName, Feature)] = features.toSet

  /**
    * Builds a feature string for a specific service.
    * @param service The service identifier to build the feature string for.
    * @param clientInfo The current client information
    * @return A feature string in the format of feature1=true|feature2=false|feature3=true
    */
  override def featureStringForService(service: String)(implicit clientInfo: ClientInfo): String = {
    val serviceFeatures: Iterable[Feature] = features.values.filter { feature: Feature =>
      feature.featureDescription match {
        case FeatureDescription(_, _, _, _, Some(services)) if services.contains(service) => true
        case _ => false
      }
    }

    FeaturesString.buildFeaturesString(serviceFeatures)
  }
}

object StaticJsonFeatureRegistry {
  def apply(classpathRelativePath: String): FeatureRegistry = {
    val jsonInputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(classpathRelativePath)
    new StaticJsonFeatureRegistry(IOUtils.toString(jsonInputStream))
  }
}
