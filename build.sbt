import BuildSettings._
import Dependencies._

// Omit the file header when using the AGPL license.
lazy val scramlFileHeader =
  """
    |All rights reserved.
    |This is a custom license header test.
  """.stripMargin


// The raml official examples are included via a git subtree and for each of them client code is generated in their own module.
lazy val helloWorldApi = Project(
  id = "raml-examples-helloworld",
  base = file("modules/raml-examples-helloworld")
).settings(
  projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "helloworld/helloworld.raml", // the path to the main raml file of the api
      scramlBaseDir in scraml in Compile :=
        file("modules/raml-examples/src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      scramlLanguage in scraml in Compile := "scala", // this is the default in the sbt plugin, so can be omitted
      scramlApiPackage in scraml in Compile := "org.raml.examples.helloworld", // our path to the main raml file is too short to use it as a package name (helloworld/helloworld.raml), so we have to declare our own package name
      scramlClassHeader in scraml in Compile := scramlFileHeader
    )
)

lazy val ramlTypescript = Project(
  id = "raml-typescript",
  base = file("modules/raml-typescript")
).settings(
  projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "io/atomicbits/raml10/RamlTestClient.raml", // the path to the main raml file of the api
      scramlBaseDir in scraml in Compile :=
        file("src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      scramlLanguage in scraml in Compile := "TypeScript",
      scramlApiPackage in scraml in Compile := "io.atomicbits", // our path to the main raml file is too short to use it as a package name (helloworld/helloworld.raml), so we have to declare our own package name
      scramlClassHeader in scraml in Compile := scramlFileHeader,
      scramlDestinationDir in scraml in Compile := file("modules/raml-typescript/src/public"),
      scramlSingleSourceFile in scraml in Compile := "helloworld.d.ts"
    )
)

lazy val ramlHtmlDoc = Project(
  id = "raml-htmldoc",
  base = file("modules/raml-htmldoc")
).settings(
  projSettings(scramlDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "io/atomicbits/raml10/RamlTestClient.raml", // the path to the main raml file of the api
      scramlBaseDir in scraml in Compile :=
        file("src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      scramlLanguage in scraml in Compile := "HtmlDoc",
      scramlClassHeader in scraml in Compile := "",
      scramlDestinationDir in scraml in Compile := file("modules/raml-htmldoc/src/public")
    )
)

// This is the main project with the official Scraml tests.
lazy val root = Project(
  id = "scraml-test-scala",
  base = file(".")
).settings(
  projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "io/atomicbits/raml10/RamlTestClient.raml",
      scramlClassHeader in scraml in Compile := scramlFileHeader
    )
).aggregate(helloWorldApi, ramlTypescript, ramlHtmlDoc)
