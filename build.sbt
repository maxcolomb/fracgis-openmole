name := "fracgis-openmole"

version := "1.0"

scalaVersion := "2.12.4"

enablePlugins(SbtOsgi)

OsgiKeys.exportPackage := Seq("fracgisplugin.*")

OsgiKeys.importPackage := Seq("*;resolution:=optional")

OsgiKeys.privatePackage := Seq("!scala.*,!java.*,META-INF.services.*,META-INF.*,*")

OsgiKeys.requireCapability := """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.8))""""

excludeFilter in unmanagedSources := HiddenFileFilter || "*CompData.java" || "*MouvData.java" || "*MouvGrid.java" || "*TestStabilite.java" || "*TotalTests.java"

resolvers += "IDB" at "http://igetdb.sourceforge.net/maven2-repository/"

resolvers += Resolver.mavenLocal

resolvers += "IGN snapshots" at "https://forge-cogit.ign.fr/nexus/content/repositories/snapshots"

resolvers += "IGN releases" at "https://forge-cogit.ign.fr/nexus/content/repositories/releases"

resolvers += "ImageJ" at "http://maven.imagej.net/content/repositories/public"

resolvers += "Boundless" at "http://repo.boundlessgeo.com/main"

resolvers += "osgeo" at "http://download.osgeo.org/webdav/geotools/"

resolvers += "geosolutions" at "http://maven.geo-solutions.it/"

resolvers += "Hibernate" at "http://www.hibernatespatial.org/repository"

val fracgisVersion = "0.6.3"
val geotoolsVersion = "12.3"

libraryDependencies += "org.thema" % "fracgis" % fracgisVersion

libraryDependencies += "org.geotools" % "gt-grid" % geotoolsVersion
libraryDependencies += "org.geotools" % "gt-coverage" % geotoolsVersion

libraryDependencies += "com.google.guava" % "guava" % "17.0"

OsgiKeys.additionalHeaders :=  Map(
			   "Specification-Title" -> "Spec Title",
			   "Specification-Version" -> "Spec Version",
			   "Specification-Vendor" -> "IGN",
			   "Implementation-Title" -> "Impl Title",
			   "Implementation-Version" -> "Impl Version",
			   "Implementation-Vendor" -> "IGN"
)
