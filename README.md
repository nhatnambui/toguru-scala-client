# FeatureBee for Scala
FeatureBee client for Scala applications

## Status
[![Build Status](https://travis-ci.org/AutoScout24/featurebee-scala.svg)](https://travis-ci.org/AutoScout24/featurebee-scala)

### Contract
The FeatureBee Server returns a list of all defined features.

    [{
      "name": "Name of the Feature",
      "description": "Some additional description",
      "tags": ["Team Name", "Or Service name"],
      "activation": [{"culture": ["de-DE"]}]
    }]

Conditions is an array of type and values. Supported types are "default", "culture", "userAgentFragments" and "trafficDistribution". 
Each condition can have its own format how the values should look like. Each condition could have multiple values. 
All conditions have to be fulfilled (logical AND). If no conditions are defined the feature is off.