resolvers += Resolver.url(
  "as24-ivy-releases",
  new URL("https://fast.services.as24.tech/artifactory/public/")
)(Resolver.ivyStylePatterns)
resolvers += "fast-releases".at(
  "https://fast.services.as24.tech/artifactory/public/"
)
credentials += (
  if (sys.env.contains("FAST_USER"))
    Credentials(
      "Artifactory Realm",
      "fast.services.as24.tech",
      sys.env("FAST_USER"),
      sys.env("FAST_TOKEN")
    )
  else Credentials(Path.userHome / ".sbt" / ".credentials")
)

addSbtPlugin("com.autoscout24" % "sbt-scalascout" % "0.6.1")

resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")

addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.2.2")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.4.0")
