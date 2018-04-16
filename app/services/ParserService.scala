package services

import javax.inject.{Inject, Singleton}
import models.LinkInfo
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ParserService @Inject()(mongoApi: ReactiveMongoApi)(implicit ctx: ExecutionContext) {


  private def collection = mongoApi.database.map(_.collection[JSONCollection]("links"))

  def save(file: String): Future[Unit] = {

    val bufferedSource = scala.io.Source.fromFile(file, "UTF8")

    val links = (for (line <- bufferedSource.getLines) yield {
      val Array(anchor, donor, acceptor) = line.split("\t", -1).map(_.trim)
      LinkInfo(acceptor, donor, anchor, id = acceptor + ":::" + donor)
    }).toList
    // TODO: bulkInsert is deprecated, need to find the better solution for multiple document insertion
    collection.flatMap { col =>
      val bulkDocs = links.map(implicitly[col.ImplicitlyDocumentProducer](_))
      col.bulkInsert(ordered = false)(bulkDocs: _*).map(_ => ())
    }

  }

  def list: Future[List[LinkInfo]] = {
    collection.flatMap(_.find(JsObject(Nil))
      .cursor[LinkInfo](ReadPreference.secondaryPreferred).collect[List](-1, Cursor.FailOnError[List[LinkInfo]]()))
  }

  def update(link: LinkInfo): Future[LinkInfo] = {
    collection.flatMap(_.findAndUpdate(Json.obj("_id" -> link.id), link, true).map(
      _.result[LinkInfo].getOrElse(throw new Exception( s"Restriction ${link.id} isn't found"))
    ))
  }


}