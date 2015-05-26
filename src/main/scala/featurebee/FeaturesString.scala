package featurebee

import featurebee.api.Feature.FeatureName

import scala.util.Try

object FeaturesString {

  private def lowerCaseKeys[T](m: Map[String,T]) = m.map { case (k, v) => (k.toLowerCase, v) }

  /**
   * Parses the string for a forced activation of features.
   *
   * @param featuresString usually has a format like: feature1=true|feature2=false|feature3=true, where the feature name should be case insensitive
   *
   * @return a function that can check if a given feature should be forced to be in the given state (true| false). The feature name is case insensitive.
   */
  def parseForcedFeaturesString(featuresString: String): FeatureName => Option[Boolean] = {
    featureName =>
      val map = featuresString.split('|').toList.map {
        singleFeature =>
          singleFeature.split('=').toList match {
            case key :: value :: Nil if Try(value.toBoolean).isSuccess => Some(key.toLowerCase -> value.toBoolean)
            case other => None // wrong feature format. e.g. name=uh
          }
      }.flatten.toMap

      lowerCaseKeys(map).get(featureName.toLowerCase)
  }
}
