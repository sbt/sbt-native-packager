sbtPlugin := true

name := "sbt-native-packager"

organization := "com.typesafe"

version := "0.2.0"

scalacOptions in Compile += "-deprecation"

seq(ghpages.settings:_*)

git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

seq(com.jsuereth.sbtsite.SitePlugin.site.settings:_*)

com.jsuereth.sbtsite.SiteKeys.siteMappings <<= (com.jsuereth.sbtsite.SiteKeys.siteMappings, baseDirectory, target, streams) map { (mappings, dir, out, s) => 
  val sphinxSrc = dir / "src" / "sphinx"
  val sphinxOut = out / "sphinx"
  // Run Jekyll
  sbt.Process(Seq("sphinx-build", "-b", "html", sphinxSrc.getAbsolutePath, sphinxOut.getAbsolutePath), Some(sphinxSrc)).!;
  // Figure out what was generated.
  mappings ++ (sphinxOut ** ("*.html" | "*.png" | "*.js" | "*.css" | "*.gif" ) x relativeTo(sphinxOut))
}

publishTo := Some(Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

publishMavenStyle := false
