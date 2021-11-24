#!/bin/bash

set -euo pipefail

sbt \
    formatCheck \
    "all test package"
