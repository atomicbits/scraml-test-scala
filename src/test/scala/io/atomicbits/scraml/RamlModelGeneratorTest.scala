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

import java.io._

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import io.atomicbits.schema._
import io.atomicbits.scraml.dsl.{BinaryData, Response, StringPart}
import io.atomicbits.scraml.TestClient01._
import io.atomicbits.scraml.dsl.client.ClientConfig
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import play.api.libs.json.{JsValue, JsString, Json, Format}

import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.{postfixOps, reflectiveCalls}

import scala.concurrent.duration._

/**
  * Created by peter on 17/05/15, Atomic BITS bvba (http://atomicbits.io).
  */
class RamlModelGeneratorTest extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val port = 8281
  val host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val client = TestClient01(
    host = host,
    port = port,
    protocol = "http",
    defaultHeaders = Map(), // "Accept" -> "application/vnd-v1.0+json"
    prefix = None,
    config = ClientConfig(),
    clientFactory = None
  )

  override def beforeAll() = {
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterAll() = {
    wireMockServer.stop()
    client.close()
  }

  feature("Use the DSL based on a RAML specification") {

    val userResource = client.rest.user
    val userFoobarResource = userResource.userid("foobar")

    scenario("test a GET request") {

      Given("a matching web service")

      stubFor(
        get(urlEqualTo(s"/rest/user?age=51.0&firstName=John%20C&organization=ESA&organization=NASA"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(
            aResponse()
              .withBody( """[{"address": {"streetAddress": "Mulholland Drive", "city": "LA", "state": "California"}, "firstName":"John", "lastName": "Doe", "age": 21, "id": "1", "other": {"text": "foobar"}}]""")
              .withStatus(200)))


      When("execute a GET request")

      val eventualUserResponse: Future[List[User]] =
        userResource
          .get(age = Some(51), firstName = Some("John C"), lastName = None, organization = List("ESA", "NASA"))
          .asType


      Then("we should get the correct user object")

      val user = User(
        homePage = None,
        address = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
        age = 21,
        firstName = "John",
        lastName = "Doe",
        id = "1",
        other = Some(Json.obj("text" -> JsString("foobar")))
      )
      val userResponse = Await.result(eventualUserResponse, 2 seconds)
      assertResult(user)(userResponse.head)
    }


    scenario("test a form POST request") {

      Given("a matching web service")

      stubFor(
        post(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
          .withHeader("Accept", equalTo("*/*"))
          .withRequestBody(equalTo("""text=Hello-Foobar""")) // """text=Hello%20Foobar"""
          .willReturn(
          aResponse()
            .withBody("Post OK")
            .withStatus(200)
        )
      )



      When("execute a form POST request")

      val eventualPostResponse: Future[String] =
        userFoobarResource
          .post(text = "Hello-Foobar", value = None).asString



      Then("we should get the correct response")

      val postResponse = Await.result(eventualPostResponse, 2 seconds)
      assertResult("Post OK")(postResponse)

    }


    scenario("test a PUT request") {

      Given("a matching web service")

      val user = User(
        homePage = Some(Link("http://foo.bar", Method.GET, None)),
        address = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
        age = 21,
        firstName = "John",
        lastName = "Doe",
        id = "1"
      )

      val link = Link("http://foo.bar", Method.GET, None)

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
          .contentApplicationVndV10Json
          .put(user)
          .asType


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

      val eventualDeleteResponse: Future[String] = userFoobarResource.addHeaders("Accept" -> "application/vnd-v1.0+json").delete().asString


      Then("we should get the correct response")

      val deleteResponse = Await.result(eventualDeleteResponse, 2 seconds)
      assertResult("Delete OK")(deleteResponse)


    }


    scenario("test a multipart/form-data POST request") {

      // http://localhost:8281/rest/user/upload	POST	headers:	Accept:application/vnd-v1.0+json	Content-Type:multipart/form-data
      Given("a form upload web service")
      stubFor(
        post(urlEqualTo(s"/rest/user/up-load"))
          .withHeader("Content-Type", equalTo("multipart/form-data"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(
            aResponse()
              .withBody("Post OK")
              .withStatus(200)
          )
      )

      When("a multipart/form-data POST request happens")
      val multipartFormPostResponse =
        client.rest.user.upload.post(List(StringPart(name = "test", value = "string part value"))).asType

      Then("we should get the correct response")

      //      val putResponse = Await.result(multipartFormPostResponse, 2 seconds)
      //      assertResult("Post OK")(putResponse)
    }


    scenario("test Lists as request and response body") {

      Given("a form upload web service")

      val user = User(
        homePage = Some(Link("http://foo.bar", Method.GET, None)),
        address = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
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
          .addHeaders("Content-Type" -> "application/vnd-v1.0+json")
          .put(List(user))
          .asType

      Then("we should get the correct response")
      val listBody = Await.result(listBodyResponse, 2 seconds)
      assertResult(List(user))(listBody)
    }


    scenario("test List as request with primitive type 'String'") {

      Given("a form upload web service")
      val dogs = List(
        Dog(gender = "female", canBark = true, name = Some("Ziva")),
        Dog(gender = "male", canBark = true, name = Some("Olly"))
      )

      def dogListToJson()(implicit formatter: Format[List[Animal]]) = {
        formatter.writes(dogs).toString()
      }

      stubFor(
        post(urlEqualTo(s"/rest/animals"))
          .withRequestBody(equalTo( """["1","2"]"""))
          .willReturn(
            aResponse()
              .withBody(dogListToJson())
              .withStatus(200)
          )
      )


      When("web service requesting an animal")
      val eventualAnimals = client.rest.animals.post(List("1", "2")).asType


      Then("we should get two dogs in the list")
      val animals: List[Animal] = Await.result(eventualAnimals, 2 seconds)
      assertResult(animals)(dogs)
    }


    scenario("test the use of a class hierarchy") {

      Given("a web service providing a dog as an animal")
      val dogs = List(Dog(gender = "female", canBark = true, name = Some("Ziva")))

      def dogListToJson()(implicit formatter: Format[List[Animal]]) = {
        formatter.writes(dogs).toString()
      }

      stubFor(
        get(urlEqualTo(s"/rest/animals"))
          .willReturn(
            aResponse()
              .withBody(dogListToJson())
              .withStatus(200)
          )
      )


      When("web service requesting an animal")
      val eventualAnimals = client.rest.animals.get().asType


      Then("we should get a dog")
      val animal: List[Animal] = Await.result(eventualAnimals, 2 seconds)
      assertResult(animal)(dogs)
    }


    scenario("test the use generic classes") {

      Given("a web service providing the dogs of a user")
      val dog = Dog(gender = "female", canBark = true, name = Some("Ziva"))
      val pagedList = PagedList[Dog, String](count = 1, elements = List(dog), owner = Option("Peter"))

      /**
        * val json = s"""{"count":1,"elements":[{"gender":"female","canBark":true,"name":"Ziva"}],"owner":"Peter"}"""
        */
      def pagedListToJson()(implicit formatter: Format[PagedList[Dog, String]]) = {
        formatter.writes(pagedList).toString()
      }


      stubFor(
        get(urlEqualTo(s"/rest/user/123/dogs"))
          .willReturn(
            aResponse()
              .withBody(pagedListToJson())
              .withStatus(200)
          )
      )


      When("web service requesting the dogs of a user")
      val eventualDogs = client.rest.user.userid("123").dogs.get().asType


      Then("we should get a paged list containing the dogs of the requested user")
      val dogs: PagedList[Dog, String] = Await.result(eventualDogs, 2 seconds)
      assertResult(dogs)(pagedList)

      println(s"dogs: $dogs")

    }


    scenario("get a tree structure") {

      Given("a service that returns a tree structure")

      stubFor(
        get(urlEqualTo(s"/rest/user/tree"))
          .willReturn(
            aResponse()
              .withBody( """{"value":"foo","children":[{"value":"bar","children":[]},{"value":"baz","children":[]}]}""")
              .withStatus(200)
          )
      )


      When("a client requests the tree")

      val eventualTree = client.rest.user.tree.get().asType

      Then("then the right tree should be received")

      val expectedTree = Tree(value = "foo", children = List(Tree(value = "bar", children = List()), Tree(value = "baz", children = List())))
      val receivedTree = Await.result(eventualTree, 2 seconds)
      println(s"tree: $receivedTree")
      assertResult(expectedTree)(receivedTree)
    }


    scenario("binary upload using a file") {

      Given("a service receives binary data")

      stubFor(
        post(urlEqualTo(s"/rest/animals/datafile/upload"))
          .withRequestBody(equalTo(new String(binaryData)))
          .willReturn(
            aResponse()
              .withBody( """{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val file = new File(this.getClass.getResource("/io/atomicbits/scraml/binaryData.bin").toURI)
      val eventualResponse: Future[Response[JsValue]] = client.rest.animals.datafile.upload.post(file)

      Then("then the data should be uploaded")
      val response = Await.result(eventualResponse, 2 seconds)
      assertResult(200)(response.status)
    }


    scenario("binary upload using an inputstream") {

      Given("a service receives binary data")

      stubFor(
        post(urlEqualTo(s"/rest/animals/datafile/upload"))
          .withRequestBody(equalTo(new String(binaryData)))
          .willReturn(
            aResponse()
              .withBody( """{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val inputStream: InputStream = this.getClass.getResourceAsStream("/io/atomicbits/scraml/binaryData.bin")
      val eventualResponse: Future[Response[JsValue]] = client.rest.animals.datafile.upload.post(inputStream)

      Then("then the data should be uploaded")
      val response = Await.result(eventualResponse, 2 seconds)
      assertResult(200)(response.status)
    }


    scenario("binary upload using a byte array") {

      Given("a service receives binary data")

      stubFor(
        post(urlEqualTo(s"/rest/animals/datafile/upload"))
          .withRequestBody(equalTo(new String(binaryData)))
          .willReturn(
            aResponse()
              .withBody( """{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val inputStream: InputStream = this.getClass.getResourceAsStream("/io/atomicbits/scraml/binaryData.bin")
      val array = new Array[Byte](1024)
      inputStream.read(array, 0, 1024)
      inputStream.close()
      val eventualResponse: Future[Response[JsValue]] = client.rest.animals.datafile.upload.post(array)

      Then("then the data should be uploaded")
      val response = Await.result(eventualResponse, 2 seconds)
      assertResult(200)(response.status)
    }


    scenario("binary upload using a string") {

      Given("a service receives binary data")

      val text = "some test string"

      stubFor(
        post(urlEqualTo(s"/rest/animals/datafile/upload"))
          .withRequestBody(equalTo(text))
          .willReturn(
            aResponse()
              .withBody( """{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val eventualResponse: Future[Response[JsValue]] = client.rest.animals.datafile.upload.post(text)

      Then("then the right data should be uploaded")
      val response = Await.result(eventualResponse, 2 seconds)
      assertResult(200)(response.status)
    }


    scenario("download binary data") {

      Given("a service responds with binary data")

      stubFor(
        get(urlEqualTo(s"/rest/animals/datafile/download"))
          .willReturn(
            aResponse()
              .withBody(binaryData)
              .withStatus(200)
          )
      )

      When("a client requests the download")
      val eventualResponse: Future[Response[BinaryData]] = client.rest.animals.datafile.download.get()

      Then("then the data should be downloaded")
      val response = Await.result(eventualResponse, 2 seconds)
      assertResult(200)(response.status)
      assertResult(binaryData)(response.body.get.asBytes)
    }

  }


  private def binaryData: Array[Byte] = {
    val data = 0 to 1023 map (_.toByte)
    Array[Byte](data: _*)
  }

  private def createBinaryDataFile = {
    val file = new File("binaryData.bin")
    val fileOutputStream = new FileOutputStream(file)
    fileOutputStream.write(binaryData)
    fileOutputStream.flush()
    fileOutputStream.close()
  }

}
