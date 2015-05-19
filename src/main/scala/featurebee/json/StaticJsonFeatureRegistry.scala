package featurebee.json

import featurebee.api.Feature.FeatureName
import featurebee.api.{FeatureImpl, Feature, FeatureRegistry}
import FeatureJsonProtocol._
import featurebee.impl.FeatureDescription
import org.apache.commons.io.IOUtils
import spray.json.DefaultJsonProtocol
import spray.json._

class StaticJsonFeatureRegistry(json: String) extends FeatureRegistry {

  private val features: Map[FeatureName, Feature] = json.parseJson.convertTo[Seq[FeatureDescription]].
    map(desc => desc.name -> new FeatureImpl(desc)).toMap
  
  override def feature(name: String): Option[Feature] = features.get(name)
  override def allFeatures: Set[Feature] = features.values.toSet
}

object StaticJsonFeatureRegistry {
  def apply(classpathRelativePath: String): FeatureRegistry = {
    val jsonInputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(classpathRelativePath)
    new StaticJsonFeatureRegistry(IOUtils.toString(jsonInputStream))
  }
}
