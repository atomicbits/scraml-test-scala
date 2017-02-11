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
import java.nio.charset.Charset

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import io.atomicbits.raml10._
import io.atomicbits.scraml.dsl.{ BinaryData, Response, StringPart }
import io.atomicbits.raml10.RamlTestClient._
import io.atomicbits.scraml.dsl.client.ClientConfig
import org.scalatest.{ BeforeAndAfterAll, FeatureSpec, GivenWhenThen }
import org.scalatest.Matchers._
import play.api.libs.json.{ Format, JsString, JsValue, Json }

import scala.concurrent.{ Await, Future }
import scala.language.{ postfixOps, reflectiveCalls }
import scala.concurrent.duration._

/**
  * Created by peter on 17/05/15, Atomic BITS bvba (http://atomicbits.io).
  */
class RamlModelGeneratorTest extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val port = 8281
  val host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  val client = RamlTestClient(
    host           = host,
    port           = port,
    protocol       = "http",
    defaultHeaders = Map(), // "Accept" -> "application/vnd-v1.0+json"
    prefix         = None,
    config         = ClientConfig(requestCharset = Charset.forName("UTF-8")),
    clientFactory  = None
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

    val userResource       = client.rest.user
    val userFoobarResource = userResource.userid("foobar")

    scenario("test a successful GET request") {

      Given("a matching web service")

      // '[]' url-encoded gives: %5B%5D
      stubFor(
        get(urlEqualTo(s"/rest/user?organization%5B%5D=ESA&organization%5B%5D=NASA&age=51.0&firstName=John%20C"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(aResponse()
            .withBody("""[{"address": {"streetAddress": "Mulholland Drive", "city": "LA", "state": "California"}, "firstName":"John", "lastName": "Doe", "age": 21, "id": "1"}]""")
            .withStatus(200)))

      When("execute a GET request")

      val eventualUserResponse: Future[List[User]] =
        userResource
          .get(age = Some(51), firstName = Some("John C"), lastName = None, organization = List("ESA", "NASA"))
          .asType

      Then("we should get the correct user object")

      val user = User(
        homePage   = None,
        address    = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
        age        = 21,
        firstName  = "John",
        lastName   = "Doe",
        id         = "1",
        other      = None, // Some(Json.obj("text" -> JsString("foobar")))
        fancyfield = None
      )
      val userResponse = Await.result(eventualUserResponse, 2 seconds)
      assertResult(user)(userResponse.head)
    }

    scenario("test a failed GET request") {

      Given("a matching web service")

      val errorMessage = "Oops"

      // '[]' url-encoded gives: %5B%5D
      stubFor(
        get(urlEqualTo(s"/rest/user?organization%5B%5D=ESA&organization%5B%5D=NASA&age=51.0&firstName=John%20C"))
          .withHeader("Accept", equalTo("application/vnd-v1.0+json"))
          .willReturn(aResponse()
            .withBody(errorMessage)
            .withStatus(500)))

      When("execute a GET request")

      val eventualUserResponse: Future[Response[List[User]]] =
        userResource
          .get(age = Some(51), firstName = Some("John C"), lastName = None, organization = List("ESA", "NASA"))

      Then("we should get the failure message")

      val userResponse = Await.result(eventualUserResponse, 2 seconds)
      assertResult(errorMessage)(userResponse.stringBody.get)
      assertResult(500)(userResponse.status)
    }

    scenario("test a form POST request") {

      Given("a matching web service")

      stubFor(
        post(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
          .withHeader("Accept", equalTo("application/json")) // We expect the default media type here!
          .withRequestBody(equalTo("""text=Hello-Foobar""")) // """text=Hello%20Foobar"""
          .willReturn(
            aResponse()
              .withBody("{\"message\": \"Post OK\"}")
              .withStatus(200)
          )
      )

      When("execute a form POST request")

      val eventualPostResponse: Future[String] =
        userFoobarResource
          .post(text = "Hello-Foobar", value = None)
          .asString

      Then("we should get the correct response")

      val postResponse = Await.result(eventualPostResponse, 2 seconds)
      assertResult("{\"message\": \"Post OK\"}")(postResponse)

    }

    scenario("test a PUT request") {

      Given("a matching web service")

      val user = User(
        homePage   = Some(Link("http://foo.bar", Method.GET, None)),
        address    = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
        age        = 21,
        firstName  = "John",
        lastName   = "Doe",
        id         = "1",
        fancyfield = None
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
          .withHeader("Content-Type", equalTo("application/vnd-v1.0+json; charset=UTF-8"))
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
        userFoobarResource.contentApplicationVndV10Json
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
              .withStatus(200)
          )
      )

      When("execute a DELETE request")

      val eventualDeleteResponse: Future[Response[JsValue]] =
        userFoobarResource.addHeaders("Accept" -> "application/vnd-v1.0+json").delete()

      Then("we should get the correct response")

      val deleteResponse = Await.result(eventualDeleteResponse, 2 seconds)
      assertResult(200)(deleteResponse.status)
    }

    scenario("test a set header request") {

      Given("a matching web service")

      stubFor(
        delete(urlEqualTo(s"/rest/user/foobar"))
          .withHeader("Accept", equalTo("foo/bar"))
          .willReturn(
            aResponse()
              .withStatus(200)
          )
      )

      When("execute a DELETE request")

      val eventualDeleteResponse: Future[Response[JsValue]] =
        userResource
          .addHeaders("Accept" -> "application/vnd-v1.0+json")
          .userid("foobar")
          .setHeaders("Accept" -> "foo/bar")
          .delete()

      Then("we should get the correct response")

      val deleteResponse = Await.result(eventualDeleteResponse, 2 seconds)
      assertResult(200)(deleteResponse.status)
    }

    scenario("test a multipart/form-data POST request") {

      // http://localhost:8281/rest/user/upload	POST	headers:	Accept:application/vnd-v1.0+json	Content-Type:multipart/form-data
      Given("a form upload web service")
      stubFor(
        post(urlEqualTo(s"/rest/user/up-load"))
          .withHeader("Content-Type", equalTo("multipart/form-data; charset=UTF-8"))
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
        homePage   = Some(Link("http://foo.bar", Method.GET, None)),
        address    = Some(UserDefinitionsAddress(streetAddress = "Mulholland Drive", city = "LA", state = "California")),
        age        = 21,
        firstName  = "John",
        lastName   = "Doe",
        id         = "1",
        fancyfield = None
      )

      // Imports needed to get the implicit JSON formatters for both types.
      import User._

      def userToJson()(implicit formatter: Format[List[User]]) = {
        formatter.writes(List(user)).toString()
      }

      println(s"user: ${userToJson()}")

      stubFor(
        put(urlEqualTo(s"/rest/user/activate"))
          .withHeader("Content-Type", equalTo("application/vnd-v1.0+json; charset=UTF-8"))
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
        Dog(gender = "male", canBark   = true, name = Some("Olly"))
      )

      def dogListToJson()(implicit formatter: Format[List[Animal]]) = {
        formatter.writes(dogs).toString()
      }

      stubFor(
        post(urlEqualTo(s"/rest/animals"))
          .withRequestBody(equalTo("""["1","2"]"""))
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

    scenario("serializing a subclass directly should have the type dicriminator in its json format") {

      Given("a webservice that takes a post request")
      val dog = Dog(gender = "female", canBark = true, name = Some("Ziva"))

      def dogToJson()(implicit formatter: Format[Dog]) = {
        formatter.writes(dog).toString()
      }

      stubFor(
        post(urlEqualTo(s"/rest/user/124/dogs"))
          .withRequestBody(equalToJson("""{"canBark": true, "gender": "female", "name": "Ziva", "_type": "Dog"}"""))
          .willReturn(
            aResponse()
              .withStatus(201)
          )
      )

      val dogPostResult = client.rest.user.userid("124").dogs.post(dog)
      val response      = Await.result(dogPostResult, 2 seconds)

      assertResult(response.status)(201)
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
      val dog       = Dog(gender                   = "female", canBark = true, name       = Some("Ziva"))
      val pagedList = PagedList[Dog, String](count = 1, elements       = List(dog), owner = Option("Peter"))

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
              .withBody("""{"value":"foo","children":[{"value":"bar","children":[]},{"value":"baz","children":[]}]}""")
              .withStatus(200)
          )
      )

      When("a client requests the tree")

      val eventualTree = client.rest.user.tree.get().asType

      Then("then the right tree should be received")

      val expectedTree =
        Tree(value = "foo", children = List(Tree(value = "bar", children = List()), Tree(value = "baz", children = List())))
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
              .withBody("""{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val file                                        = new File(this.getClass.getResource("/io/atomicbits/scraml/binaryData.bin").toURI)
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
              .withBody("""{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val inputStream: InputStream                    = this.getClass.getResourceAsStream("/io/atomicbits/scraml/binaryData.bin")
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
              .withBody("""{"received":"OK"}""")
              .withStatus(200)
          )
      )

      When("a client uploads the data")
      val inputStream: InputStream = this.getClass.getResourceAsStream("/io/atomicbits/scraml/binaryData.bin")
      val array                    = new Array[Byte](1024)
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
              .withBody("""{"received":"OK"}""")
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

  feature("Use the DSL based on a RAML 1.0 specification") {

    // Regular Book
    scenario("test a GET request to get a Book list (the base class of a hierarchy)") {

      Given("a web service that returns a list of books")
      val booksResource = client.books

      stubFor(
        get(urlEqualTo(s"/books"))
          .withHeader("Accept", equalTo("application/json"))
          .willReturn(aResponse()
            .withBody("""[{"author": {"firstName": "James", "lastName": "Corey"}, "isbn":"978-0-316-12908-4", "title": "Leviathan Wakes", "genre": "SciFi", "type": "Book"}, {"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "type": "ComicBook"}, {"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "era": "1990", "type": "SciFiComicBook"}]""")
            .withStatus(200)))

      When("we request the list of books")
      val futureBooks       = booksResource.get.asType
      val books: List[Book] = Await.result(futureBooks, 2 seconds)

      Then("we should get the expected books")
      //.head.author shouldBe Author(firstName = "James", lastName = "Corey")
      val expectedBooks = Set(
        BookImpl(title        = "Leviathan Wakes",
                 author       = Author(firstName = "James", lastName = "Corey"),
                 genre        = "SciFi",
                 isbn         = "978-0-316-12908-4"),
        ComicBookImpl(title   = "The Clone Conspiracy",
                      author  = Author(firstName = "Peter", lastName = "David"),
                      genre   = "SciFi",
                      isbn    = "75960608623800111",
                      hero    = "Spiderman"),
        SciFiComicBook(title  = "The Clone Conspiracy",
                       author = Author(firstName = "Peter", lastName = "David"),
                       genre  = "SciFi",
                       isbn   = "75960608623800111",
                       hero   = "Spiderman",
                       era    = "1990")
      )
      assertResult(expectedBooks)(books.toSet)

    }

    scenario("test a POST request with a Book (the base class of a hierarchy)") {

      Given("a web service that receives a list of books")
      val booksResource = client.books

      stubFor(
        post(urlEqualTo(s"/books"))
          .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
          .withRequestBody(
            equalToJson(
              """{"author": {"firstName": "James", "lastName": "Corey"}, "isbn":"978-0-316-12908-4", "title": "Leviathan Wakes", "genre": "SciFi", "type": "Book"}""")
          )
          .willReturn(aResponse()
            .withStatus(201)))

      When("we post a book")
      val book = BookImpl(title = "Leviathan Wakes",
                          author = Author(firstName = "James", lastName = "Corey"),
                          genre  = "SciFi",
                          isbn   = "978-0-316-12908-4")
      val futureBookResponse = booksResource.post(book)

      Then("we should get the expected success response")
      val response = Await.result(futureBookResponse, 2 seconds)
      response.status shouldBe 201
    }

    // Comic Book
    scenario("test a GET request to get a ComicBook list (a non-leaf subclass in a class hierarchy)") {

      Given("a web service that returns a list of comic books")
      val comicBooksResource = client.books.comicbooks

      stubFor(
        get(urlEqualTo(s"/books/comicbooks"))
          .withHeader("Accept", equalTo("application/json"))
          .willReturn(aResponse()
            .withBody("""[{"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "type": "ComicBook"}]""")
            .withStatus(200)))

      When("we request the list of comic books")
      val futureBooks            = comicBooksResource.get.asType
      val books: List[ComicBook] = Await.result(futureBooks, 2 seconds)

      Then("we should get the expected comic books")
      books.head.author shouldBe Author(firstName = "Peter", lastName = "David")

    }

    scenario("test a POST request with a ComicBook (a non-leaf subclass in a class hierarchy)") {

      Given("a web service that receives a list of comic books")
      val comicBooksResource = client.books.comicbooks

      stubFor(
        post(urlEqualTo(s"/books/comicbooks"))
          .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
          .withRequestBody(
            equalToJson(
              """{"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "type": "ComicBook"}"""
            )
          )
          .willReturn(
            aResponse()
              .withStatus(201)
          )
      )

      When("we post a comic book")
      val book = ComicBookImpl(title = "The Clone Conspiracy",
                               author = Author(firstName = "Peter", lastName = "David"),
                               genre  = "SciFi",
                               isbn   = "75960608623800111",
                               hero   = "Spiderman")
      val futureBookResponse = comicBooksResource.post(book)

      Then("we should get the expected success response")
      val response = Await.result(futureBookResponse, 2 seconds)
      response.status shouldBe 201
    }

    // SciFi Comic Book
    scenario("test a GET request to get a SciFi ComicBook list (a leaf subclass in a class hierarchy)") {

      Given("a web service that returns a list of comic books")
      val scifiComicBooksResource = client.books.comicbooks.scificomicbooks

      stubFor(
        get(urlEqualTo(s"/books/comicbooks/scificomicbooks"))
          .withHeader("Accept", equalTo("application/json"))
          .willReturn(aResponse()
            .withBody("""[{"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "era": "1990", "type": "SciFiComicBook"}]""")
            .withStatus(200)))

      When("we request the list of scifi comic books")
      val futureBooks            = scifiComicBooksResource.get.asType
      val books: List[ComicBook] = Await.result(futureBooks, 2 seconds)

      Then("we should get the expected comic books")
      books.head.author shouldBe Author(firstName = "Peter", lastName = "David")

    }

    scenario("test a POST request with a SciFi ComicBook (a leaf subclass in a class hierarchy)") {

      Given("a web service that receives a list of comic books")
      val scifiComicBooksResource = client.books.comicbooks.scificomicbooks

      stubFor(
        post(urlEqualTo(s"/books/comicbooks/scificomicbooks"))
          .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
          .withRequestBody(
            equalToJson(
              """{"author": {"firstName": "Peter", "lastName": "David"}, "isbn":"75960608623800111", "title": "The Clone Conspiracy", "genre": "SciFi", "hero": "Spiderman", "era": "1990", "type": "SciFiComicBook"}"""
            )
          )
          .willReturn(
            aResponse()
              .withStatus(201)
          )
      )

      When("we post a SciFi comic book")
      val book = SciFiComicBook(title = "The Clone Conspiracy",
                                author = Author(firstName = "Peter", lastName = "David"),
                                genre  = "SciFi",
                                isbn   = "75960608623800111",
                                hero   = "Spiderman",
                                era    = "1990")
      val futureBookResponse = scifiComicBooksResource.post(book)

      Then("we should get the expected success response")
      val response = Await.result(futureBookResponse, 2 seconds)
      response.status shouldBe 201
    }

  }

  private def binaryData: Array[Byte] = {
    val data = 0 to 1023 map (_.toByte)
    Array[Byte](data: _*)
  }

  private def createBinaryDataFile = {
    val file             = new File("binaryData.bin")
    val fileOutputStream = new FileOutputStream(file)
    fileOutputStream.write(binaryData)
    fileOutputStream.flush()
    fileOutputStream.close()
  }

}
