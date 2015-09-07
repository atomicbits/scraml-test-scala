package io.atomicbits.scraml.template

import io.atomicbits.scraml.TestClient01
import io.atomicbits.scraml.TestClient01._
import io.atomicbits.scraml.dsl.client.ClientConfig


object HelloWorld {

  def main(args: Array[String]) {
    println("Hello World!!!")
  }

  def sayHello(): String = "Hello!"

  def useApi() = {
    val client = new TestClient01(
      host = "localhost",
      port = 80,
      protocol = "http",
      prefix = None,
      config = ClientConfig(),
      defaultHeaders = Map("Accept" -> "application/vnd-v1.0+json"))

    client.rest.user
      .get(age = Some(51), firstName = Some("John"), lastName = None, organization = List("ESA", "NASA"))
      .call()
      .asType
  }

}
