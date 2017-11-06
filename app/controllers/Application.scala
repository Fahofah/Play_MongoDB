package controllers

import javax.inject.Inject

import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection
import models.{Feed, User}
import play.api.libs.json.Json
import reactivemongo.api.Cursor

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.play.json._
import models.JsonFormats._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONDocument

class Application @Inject() (
                              val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents {

  def index = Action {Ok(views.html.index("Appliction running"))}

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("persons"))

  def create = Action.async {
    val user = User(29, "John", "Smith", List(
      Feed("Slashdot news", "http://slashdot.org/slashdot.rdf")))

    val futureResult = collection.flatMap(_.insert(user))

    futureResult.map(_ => Ok)
  }

  def findByName(lastName: String) = Action.async {
    val cursor: Future[Cursor[User]] = collection.map {
      _.find(Json.obj("lastName" -> lastName)).
        sort(Json.obj("created" -> -1)).
        cursor[User]
    }
    val futureUsersList: Future[List[User]] = cursor.flatMap(_.collect[List]())
    futureUsersList.map { persons =>
      Ok(persons.toString)
    }
  }


  def updatePerson(lastName: String) =  Action.async {

    val selector = BSONDocument("lastName" -> lastName)


    val newUser = User(29, "John", "RObrt", List(
      Feed("Slashdot news", "http://slashdot.org/slashdot.rdf")))

    // get a future update
    val futureUpdate1 = collection.map(_.findAndUpdate(selector,newUser))
    futureUpdate1.map { result =>
      Ok
    }
  }

  def deletePerson(lastName: String) =  Action.async {

    val selector = BSONDocument("lastName" -> lastName)

    // get a future delete
    val futureDelete = collection.map(_.remove(selector))
    futureDelete.map { result =>
      Ok
    }
  }
}