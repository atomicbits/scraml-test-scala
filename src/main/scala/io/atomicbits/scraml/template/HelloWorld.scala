package io.atomicbits.scraml.template

import io.atomicbits.scraml.TestClient01


object HelloWorld {

  def main(args: Array[String]) {
    println("Hello World!!!")
  }

  def sayHello(): String = "Hello!"

  def useApi() = {
    val client = TestClient01(host = "localhost", port = 80,
      defaultHeaders = Map("Accept" -> "application/vnd-v1.0+json"))
    client.rest.user
      .get(age = Some(51), firstName = Some("John"), lastName = None, organization = List("ESA", "NASA"))
      .call()
      .asType
  }

}
