# FeatureBee for Scala
FeatureBee client for scala applications

## Status
[![Build Status](https://travis-ci.org/AutoScout24/featurebee-scala.svg)](https://travis-ci.org/AutoScout24/featurebee-scala)

### Contract
The FeatureBee Server returns a list of all defined features.

    [{
      "name": "Name of the Feature",
      "description": "Some additional description",
      "tags": ["Team Name", "Or Service name"],
      "state": "inDevelopment",
      "conditions": [{"culture": ["de-DE"]}]
    }]

State could be "inDevelopment", "underTest" or "released". 
Conditions is an array of type and values. Supported types are "culture", "browser" and "trafficDistribution". Each condition can have its own format how the values should look like. Each condition could have multiple values. All conditions have to be fulfilled (logical AND).
