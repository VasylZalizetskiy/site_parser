package controllers

//scalastyle:off public.methods.have.type

import java.net.UnknownHostException
import java.nio.file.{Path, Paths}
import java.util.Date

import javax.inject.{Inject, Singleton}
import models.LinkInfo
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.InjectedController
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import services.ParserService
import utils.RichJson._

@Singleton
class ParserController @Inject()(app: play.Application, parser: ParserService)(implicit exec: ExecutionContext) extends InjectedController {
  val filePath = "\\temp\\files\\"

  def acceptFile = Action.async(parse.multipartFormData) { request =>

    request.body.file("file") match {
      case Some(file) =>

        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system

        val filename = Paths.get(file.filename).getFileName
        val fullPath = app.path + filePath + filename

        file.ref.moveTo(Paths.get(fullPath), replace = true)

        parser.save(fullPath).map { _ =>
          Logger.info(s"File $filename succesesfull uploaded to DB")
          // TODO: return some response body
          NoContent
        }
      case _ => Future.successful(BadRequest)
    }
  }

  def showSavedLinksInfo = Action.async { _ =>
    parser.list.map { list =>
      Logger.info(s"Listing saved links info")

      Ok(Json.obj(
        "items" -> list.map(toJson)
      ))
    }
  }

  def parseDonorsFromDB = Action.async { _ =>
    // Parse elements from files, URLs or plain strings
    val browser = JsoupBrowser()
    // TODO: make update atomary and shorten request time
    val parsedLinks = parser.list.flatMap { list =>
      val futures = list.map { linkInfo =>
        try {
          val doc = browser.get(linkInfo.donor)
          val parsedAnchor = doc >> text(s"a[href='${linkInfo.acceptor}']")
          val updateLink = if (linkInfo.created.isEmpty) linkInfo.copy(anchorRelevance = Some(linkInfo.anchor == parsedAnchor), created = Some(new Date()), checked = Some(new Date()))
          else linkInfo.copy(anchorRelevance = Some(linkInfo.anchor == parsedAnchor), checked = Some(new Date()))
          parser.update(updateLink)
        }
        catch {
          case ex: UnknownHostException =>
            Logger.info("UnknownHostException:" + linkInfo.donor)
            val updateLink = linkInfo.copy(removed = Some(new Date()), checked = Some(new Date()))
            parser.update(updateLink)
          case ex: NoSuchElementException =>
            Logger.info("NoSuchElementException" + linkInfo.acceptor)
            val updateLink = linkInfo.copy(removed = Some(new Date()), checked = Some(new Date()))
            parser.update(updateLink)
        }
      }
      Future.sequence(futures)
    }

    parsedLinks.map { list =>
      Ok(Json.obj(
        "items" -> list.map(toJson)
      ))
    }

  }

  // TODO: implement parseDonorsDirectlyFromFile
  def parseDonorsDirectlyFromFile = ???

  private def toJson(link: LinkInfo) = Json.toJson(link).as[JsObject].without("_id")

}