/*
 * (C) Copyright 2015 Atomic BITS (http://atomicbits.io).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License
 * (AGPL) version 3.0 which accompanies this distribution, and is available in
 * the LICENSE file or at http://www.gnu.org/licenses/agpl-3.0.en.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * Contributors:
 *     Peter Rigole
 *
 */

package io.atomicbits.scraml

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import io.atomicbits.schema._
import io.atomicbits.scraml.dsl.StringPart
import io.atomicbits.scraml.TestClient01._
import io.atomicbits.scraml.dsl.client.ClientConfig
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import play.api.libs.json.Format

import scala.concurrent.{Await, Future}
import scala.language.{postfixOps, reflectiveCalls}

import scala.concurrent.duration._

/**
 * Created by peter on 17/05/15, Atomic BITS bvba (http://atomicbits.io). 
 */
class FooRamlModelGeneratorTest extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val port = 8281
  val host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  override def beforeAll() = {
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterAll() = {
    wireMockServer.stop()
  }

  feature("Use the DSL based on a RAML specification") {

    val client = new TestClient01(
      host = host,
      port = port,
      protocol = "http",
      defaultHeaders = Map("Accept" -> "application/vnd-v1.0+json"),
      prefix = None,
      config = ClientConfig()
    )

    val userResource = client.rest.user
    val userFoobarResource = userResource.userid("foobar")

    scenario("test a GET request") {

      Given("a matching web service")

      stubFor(
        get(urlEqualTo(s"/rest/user?age=51.0&firstName=John&organization=ESA&organization=NASA"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(
            aResponse()
              .withBody( """{"address": {"streetAddress": "Mulholland Drive", "city": "LA", "state": "California"}, "firstName":"John", "lastName": "Doe", "age": 21, "id": "1"}""")
              .withStatus(200)))


      When("execute a GET request")

      val eventualUserResponse: Future[User] =
        userResource
          .get(age = Some(51), firstName = Some("John"), lastName = None, organization = List("ESA", "NASA"))
          .call().asType


      Then("we should get the correct user object")

      val user = User(
        homePage = None,
        address = Some(UserDefinitionsAddress("Mulholland Drive", "LA", "California")),
        age = 21,
        firstName = "John",
        lastName = "Doe",
        id = "1"
      )
      val userResponse = Await.result(eventualUserResponse, 2 seconds)
      assertResult(user)(userResponse)

    }


    scenario("test a form POST request") {

      Given("a matching web service")

      stubFor(
        post(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .withRequestBody(equalTo( """text=Hello%20Foobar"""))
          .willReturn(
            aResponse()
              .withBody("Post OK")
              .withStatus(200)
          )
      )



      When("execute a form POST request")

      val eventualPostResponse: Future[String] =
        userFoobarResource
          .post(text = "Hello Foobar", value = None).call().asString



      Then("we should get the correct response")

      val postResponse = Await.result(eventualPostResponse, 2 seconds)
      assertResult("Post OK")(postResponse)

    }


    scenario("test a PUT request") {

      Given("a matching web service")

      val user = User(
        homePage = Some(Link("http://foo.bar", LinkMethod.GET, None)),
        address = Some(UserDefinitionsAddress("Mulholland Drive", "LA", "California")),
        age = 21,
        firstName = "John",
        lastName = "Doe",
        id = "1"
      )

      val link = Link("http://foo.bar", LinkMethod.GET, None)

      import User._
      import Link._

      def userToJson()(implicit formatter: Format[User]) = {
        formatter.writes(user).toString()
      }

      def linkToJson()(implicit formatter: Format[Link]) = {
        formatter.writes(link).toString()
      }

      stubFor(
        put(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Content-Type", equalTo("application/vnd-v1.0+json"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .withRequestBody(equalTo(userToJson()))
          .willReturn(
            aResponse()
              .withBody(linkToJson())
              .withStatus(200)
          )
      )


      When("execute a PUT request")

      val eventualPutResponse: Future[Link] =
        userFoobarResource
          .withHeaders(
            "Content-Type" -> "application/vnd-v1.0+json",
            "Accept" -> "application/vnd-v1.0+json")
          .put(user)
          .call().asType


      Then("we should get the correct response")

      val putResponse = Await.result(eventualPutResponse, 2 seconds)
      assertResult(link)(putResponse)

    }


    scenario("test a DELETE request") {

      Given("a matching web service")

      stubFor(
        delete(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(
            aResponse()
              .withBody("Delete OK")
              .withStatus(200)
          )
      )


      When("execute a DELETE request")

      val eventualPutResponse: Future[String] = userFoobarResource.delete().call().asString


      Then("we should get the correct response")

      val putResponse = Await.result(eventualPutResponse, 2 seconds)
      assertResult("Delete OK")(putResponse)


    }

    scenario("test a multipart/form-data POST request") {

      Given("a form upload web service")
      stubFor(
        post(urlEqualTo(s"/rest/user/upload"))
          .withHeader("Content-Type", equalTo("multipart/form-data"))
          .willReturn(
            aResponse()
              .withBody("Post OK")
              .withStatus(200)
          )
      )

      When("a multipart/form-data POST request happens")
      val multipartFormPostResponse =
        client.rest.user.upload.post(List(StringPart(name = "test", value = "string part value"))).call().asType

      Then("we should get the correct response")


    }

    scenario("test Lists as request and response body") {

      Given("a form upload web service")

      val user = User(
        homePage = Some(Link("http://foo.bar", LinkMethod.GET, None)),
        address = Some(UserDefinitionsAddress("Mulholland Drive", "LA", "California")),
        age = 21,
        firstName = "John",
        lastName = "Doe",
        id = "1"
      )

      // Imports needed to get the implicit JSON formatters for both types.
      import User._

      def userToJson()(implicit formatter: Format[List[User]]) = {
        formatter.writes(List(user)).toString()
      }

      println(s"user: ${userToJson()}")

      stubFor(
        put(urlEqualTo(s"/rest/user/activate"))
          .withHeader("Content-Type", equalTo("application/vnd-v1.0+json"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .withRequestBody(equalTo(userToJson()))
          .willReturn(
            aResponse()
              .withBody(userToJson())
              .withStatus(200)
          )
      )

      When("a request with list body happens")
      val listBodyResponse =
        client.rest.user.activate
          .withHeaders("Content-Type" -> "application/vnd-v1.0+json")
          .put(List(user))
          .call().asType

      Then("we should get the correct response")
      val listBody = Await.result(listBodyResponse, 2 seconds)
      assertResult(List(user))(listBody)

    }

    scenario("test the use of a class hierarchy") {

      Given("a web service providing a dog as an animal")
      val dog = Dog(gender = "female", canBark = true, name = Some("Ziva"))

      def dogToJson()(implicit formatter: Format[Animal]) = {
        formatter.writes(dog).toString()
      }

      stubFor(
        get(urlEqualTo(s"/rest/animals"))
          .willReturn(
            aResponse()
              .withBody(dogToJson())
              .withStatus(200)
          )
      )


      When("web service requesting an animal")
      val eventualAnimal = client.rest.animals.get().call().asType


      Then("we should get a dog")
      val animal: Animal = Await.result(eventualAnimal, 2 seconds)
      assertResult(animal)(dog)

      // println(s"animal: $animal")

    }


  }

}
