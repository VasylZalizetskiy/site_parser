package controllers

//scalastyle:off public.methods.have.type

import java.io.IOException
import java.nio.file.{Path, Paths}

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

@Singleton
class ParserController @Inject()(app: play.Application)(implicit exec: ExecutionContext) extends InjectedController {
  val filePath = "\\temp\\files\\"

  def test = Action(parse.multipartFormData) { request =>

    request.body.file("file") match {
      case Some(file) =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename = Paths.get(file.filename).getFileName

        file.ref.moveTo(Paths.get(app.path + filePath + filename), replace = true)

        val resL = uploadLinks(filename).map { tuple =>
          val ( savedAnchor, donor, acceptor) = tuple
          // Parse elements from files, URLs or plain strings
          val browser = JsoupBrowser()

          val currentAnchor = browser.get(donor) >> text(s"a[href='$acceptor']")

          Json.obj("anchor" -> savedAnchor, "donor" -> donor, "acceptor" -> acceptor)
        }

        Ok(Json.obj("List" -> resL.toSeq))
      case _ => BadRequest
    }
  }

  private def uploadLinks(fileName: Path) = {
    val bufferedSource = scala.io.Source.fromFile(app.path + filePath + fileName, "UTF8")
    val result = for (line <- bufferedSource.getLines) yield {
      val Array(anchor, donor, acceptor) = line.split("\t", -1).map(_.trim)
      (anchor, donor, acceptor)
    }
    //    bufferedSource.close
    result
  }

}