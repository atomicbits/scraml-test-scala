import BuildSettings._
import Dependencies._

// We're using this to test the license system. Omit the license key when using the AGPL license.
lazy val scramlTestLicense =
  """
    |U1Q7Q1ZQWnZMbHZSakQ7TGVwN0JSYkJXT2I7c29tZW9uZTsyMDE2LTA3LTIzOy0xIXNpMG9keE1k
    |M2FqeVpGb2paYWVJWEFlTWg5bWFMNS84UU84MVpMTkwwMlVvZlNhREM1a3RPVEZzckkzY09yR0VK
    |NmNnNXExZjdSaEEKNmNaMndDQlg0OGcyWDVVVkMwVWlFNjdDVmVWdXR6VnBNaTZxWVdGZ05YYXVM
    |ZlJPSDdCcEJCcjQ5VDBPU3FnVzlLYVQ5Rm5SUm5UbQpIcVVLQ2xXMnppSHZDd0lmVkR5OFk1MEE1
    |b1NEUTc5SWVDSlFPZUlVYTVkbWsrWTNJd04wZkpBZmIzZEhKKzhJTm1JZUR2aTY2QnBVClN0WFAw
    |UEFGSXJTdHlqSGtTaEpzOXNDbVBwQkZUWWpFY2tqb3pKME11VWJkVzRXWVpKVDhrbXR4c1AzZjNO
    |MXVuWWMxcW9WQlJjSnYKSWwvMjZqWDUzZURneXMvbFFOdEVYUjE1WjlyU2Y3dk00SW95V3c9PQ==
  """.stripMargin

// Omit the file header when using the AGPL license.
lazy val scramlFileHeader =
  """
    |All rights reserved.
    |This is a custom license header test.
  """.stripMargin


// The raml official examples are included via a git subtree and for each of them client code is generated in their own module.
lazy val helloWorldApi = Project(
  id = "raml-examples-helloworld",
  base = file("modules/raml-examples-helloworld"),
  settings = projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "helloworld/helloworld.raml", // the path the the main raml file of the api
      scramlBaseDir in scraml in Compile :=
        file("modules/raml-examples/src/main/resources").absolutePath, // omit when the raml files are in the module's own resources folder
      scramlLanguage in scraml in Compile := "scala", // this is the default in the sbt plugin, so can be omitted
      scramlApiPackage in scraml in Compile := "org.raml.examples.helloworld", // our path to the main raml file is too short to use it as a package name (helloworld/helloworld.raml), so we have to declare our own package name
      scramlLicenseKey in scraml in Compile := scramlTestLicense, // omit when using the AGPL license
      scramlClassHeader in scraml in Compile := scramlFileHeader // omit when using the AGPL license
    )
)



// This is the main project with the official Scraml tests.
lazy val root = Project(
  id = "scraml-test-scala",
  base = file("."),
  settings = projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "io/atomicbits/raml10/RamlTestClient.raml",
      scramlLicenseKey in scraml in Compile := scramlTestLicense, // omit when using the AGPL license
      scramlClassHeader in scraml in Compile := scramlFileHeader // omit when using the AGPL license
    )
).aggregate(helloWorldApi)
