package featurebee.impl

import featurebee.impl.FeatureDescriptionSingleton.Tag

case class FeatureDescription(name: String, description: String, tags: Set[Tag], activation: Set[Condition])  {
  if(activation.isEmpty) throw new IllegalArgumentException("""conditions for activation may not be empty, consider using AlwaysOn/AlwaysOff (in json: "default" : true/false) if you want to statically switch features on or off""")
}

object FeatureDescriptionSingleton {

  type Tag = String
}

case class FeatureConfig(featureMetas: List[FeatureDescription])
