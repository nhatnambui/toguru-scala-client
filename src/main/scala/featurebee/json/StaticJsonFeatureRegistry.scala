package featurebee.json

import featurebee.api.FeatureRegistry
import org.apache.commons.io.IOUtils
import org.scalactic._
import spray.json.JsonParser.ParsingException

@deprecated(message = "Use JsonFeatureRegistry", since = "20.05.2016")
class StaticJsonFeatureRegistry(json: String) extends JsonFeatureRegistry (
  JsonFeatureRegistry.featureDescriptions(json) match {
    case Good(featureDescriptions) => featureDescriptions
    case Bad(err) => throw new ParsingException(err.head.errorMessage)
  }
)

object StaticJsonFeatureRegistry {
  def safeApply(classpathRelativePath: String): FeatureRegistry Or JsonFeatureRegistry.Error = {
    val jsonInputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(classpathRelativePath)
    JsonFeatureRegistry(IOUtils.toString(jsonInputStream))
  }

  @deprecated(message = "use safeApply instead which has a better return type for catching parsing problems", since = "15.06.2016")
  def apply(classpathRelativePath: String): FeatureRegistry = {
    val jsonInputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(classpathRelativePath)
    JsonFeatureRegistry(IOUtils.toString(jsonInputStream)) match {
      case Good(reg) => reg
      case Bad(err) => throw new ParsingException(err.errorMessage)
    }
  }
}
