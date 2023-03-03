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
      Compile / scraml / scramlRamlApi := "helloworld/helloworld.raml", // the path to the main raml file of the api
      Compile / scraml / scramlBaseDir := file("modules/raml-examples/src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      Compile / scraml / scramlLanguage := "scala", // this is the default in the sbt plugin, so can be omitted
      Compile / scraml / scramlApiPackage := "org.raml.examples.helloworld", // our path to the main raml file is too short to use it as a package name (helloworld/helloworld.raml), so we have to declare our own package name
      Compile / scraml / scramlClassHeader := scramlFileHeader
    )
)

lazy val ramlTypescript = Project(
  id = "raml-typescript",
  base = file("modules/raml-typescript")
).settings(
  projSettings(scramlDeps ++ testDeps) ++
    Seq(
      Compile / scraml / scramlRamlApi := "io/atomicbits/raml10/RamlTestClient.raml", // the path to the main raml file of the api
      Compile / scraml / scramlBaseDir := file("src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      Compile / scraml / scramlLanguage := "TypeScript",
      Compile / scraml / scramlApiPackage := "io.atomicbits", // our path to the main raml file is too short to use it as a package name (helloworld/helloworld.raml), so we have to declare our own package name
      Compile / scraml / scramlClassHeader := scramlFileHeader,
      Compile / scraml / scramlDestinationDir := file("modules/raml-typescript/src/public"),
      Compile / scraml / scramlSingleSourceFile := "helloworld.d.ts"
    )
)

lazy val ramlHtmlDoc = Project(
  id = "raml-htmldoc",
  base = file("modules/raml-htmldoc")
).settings(
  projSettings(scramlDeps) ++
    Seq(
      Compile / scraml / scramlRamlApi := "io/atomicbits/raml10/RamlTestClient.raml", // the path to the main raml file of the api
      Compile / scraml / scramlBaseDir := file("src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      Compile / scraml / scramlLanguage := "HtmlDoc",
      Compile / scraml / scramlClassHeader := "",
      Compile / scraml / scramlDestinationDir := file("modules/raml-htmldoc/src/public")
    )
)

// This is the main project with the official Scraml tests.
lazy val root = Project(
  id = "scraml-test-scala",
  base = file(".")
).settings(
  projSettings(scramlDeps ++ testDeps) ++
    Seq(
      Compile / scraml / scramlRamlApi := "io/atomicbits/raml10/RamlTestClient.raml",
      Compile / scraml / scramlClassHeader := scramlFileHeader
    )
).aggregate(helloWorldApi, ramlTypescript, ramlHtmlDoc)
