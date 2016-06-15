package featurebee.json

import featurebee.api.Feature._
import featurebee.api.{Feature, FeatureImpl, FeatureRegistry}
import org.scalactic._
import scala.util.{Failure, Success, Try}
import FeatureJsonProtocol._
import featurebee.impl.FeatureDescription
import spray.json.DefaultJsonProtocol
import spray.json._
import org.scalactic.Accumulation._

/**
  * Creates a feature registry based on the given json feature descriptions.
  * If multiple features with the same name are defined, earlier occurrences (in a file or multiple files) have preference.
  */
class JsonFeatureRegistry(featureDescriptions: Seq[FeatureDescription]) extends FeatureRegistry {

  private val features: Map[FeatureName, Feature] = {
    val featureNames = featureDescriptions.map(_.name).toSet
    val uniqueFeatureDescriptions = featureNames.flatMap(name => featureDescriptions.find(fd => fd.name == name))
    uniqueFeatureDescriptions.map(desc => desc.name -> new FeatureImpl(desc)).toMap
  }

  override def feature(name: String): Option[Feature] = features.get(name.toLowerCase)

  override def allFeatures: Set[(FeatureName, Feature)] = features.toSet
}

object JsonFeatureRegistry {
  case class Error(json: String, errorMessage: String)

  def featureDescriptions(json:String): Seq[FeatureDescription] Or One[Error] = {
    val descriptionsTry = Try(json.parseJson.convertTo[Seq[FeatureDescription]].map(fd => fd.copy(name = fd.name.toLowerCase)))
    descriptionsTry match {
      case Success(descriptions) =>
        val featureNames: Seq[String] = descriptions.map(_.name)
        Good(featureNames.toSet.flatMap {
          name: String => descriptions.collectFirst {
            case fd@FeatureDescription(fdName, _, _, _, _) if fdName == name => fd
          }
        }.toSeq)
      case Failure(ex) => Bad(One(Error(json, ex.getClass.getSimpleName + ": " + ex.getMessage)))
    }
  }

  def apply(json: String): FeatureRegistry Or Error = apply(Seq(json)).badMap(_.head)

  def apply(jsons: Seq[String]): FeatureRegistry Or Every[Error] =
    jsons.map(json => featureDescriptions(json)).combined.map { descriptions => new JsonFeatureRegistry(descriptions.flatten) }
}
