import BuildSettings._
import Dependencies._

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

lazy val scramlFileHeader =
  """
    |All rights reserved.
    |This is a custom license header test.
  """.stripMargin

lazy val root = Project(
  id = "scraml-test-scala",
  base = file("."),
  settings = projSettings(scramlDeps ++ testDeps) ++
    Seq(
      scramlRamlApi in scraml in Compile := "io/atomicbits/raml10/RamlTestClient.raml",
      scramlLicenseKey in scraml in Compile := scramlTestLicense,
      scramlClassHeader in scraml in Compile := scramlFileHeader
    )
)
