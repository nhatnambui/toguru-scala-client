package featurebee.impl

import featurebee.impl.FeatureDescriptionSingleton.Tag

case class FeatureDescription(name: String, description: String, tags: Option[Set[Tag]], activation: Set[Condition])  {
  if(name.trim.length == 0) throw new IllegalArgumentException("Name of feature must not be empty")
  if(description.trim.length == 0) throw new IllegalArgumentException("Description of feature must not be empty")
  if(activation.isEmpty) throw new IllegalArgumentException("""conditions for activation may not be empty, consider using AlwaysOn/AlwaysOff (in json: "default" : true/false) if you want to statically switch features on or off""")
}

object FeatureDescriptionSingleton {
  type Tag = String
}