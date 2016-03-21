name := "CBDP"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies  ++= Seq(
    // other dependencies here
    "org.scalanlp" %% "breeze" % "0.11.2",
    // native libraries are not included by default. add this if you want them (as of 0.7)
    // native libraries greatly improve performance, but increase jar sizes.
    // It also packages various blas implementations, which have licenses that may or may not
    // be compatible with the Apache License. No GPL code, as best I know.
    "org.scalanlp" %% "breeze-natives" % "0.11.2",
    // the visualization library is distributed separately as well.
    // It depends on LGPL code.
    "org.scalanlp" %% "breeze-viz" % "0.11.2"
)

resolvers ++= Seq(
    // other resolvers here
    // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"
libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.5.1"
libraryDependencies += "org.jsoup" % "jsoup" % "1.8.3"
libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.5.2"
libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.4.0-M3"



// or 2.11.5
