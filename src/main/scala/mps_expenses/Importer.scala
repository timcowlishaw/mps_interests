package mps_expenses

import scala.io.{Source => IOSource }
import scala.concurrent.duration._
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import scala.collection.immutable.Seq

import akka.actor.ActorSystem
import akka.stream.{ActorFlowMaterializer, FlattenStrategy}
import akka.stream.scaladsl._
import akka.http.model._
import akka.http.Http
import akka.util.Timeout
import akka.util.ByteString

import java.io.StringReader

import org.xml.sax.InputSource
import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import de.l3s.boilerpipe.extractors.ArticleExtractor;


object Importer extends App {

  import Persistence._
  import Models._

  implicit val system = ActorSystem("mps-expenses")
  implicit val mat = ActorFlowMaterializer()
  implicit val askTimeout : Timeout = 1000.millis


  val hostname = "www.publications.parliament.uk"
  val rootDir = "http://www.publications.parliament.uk/pa/cm/cmregmem/911/"
  val indexPage = rootDir ++ "part1contents.htm"
  val linkPattern = "^[a-z\\-]+_([a-z\\-]+_?)*\\.htm$".r
  val nameXpath = "h2"
  val interestsXpath = "h2/following-sibling::*[self::p | self::h3]"


  val g = FlowGraph.closed() { implicit b =>
    import FlowGraph.Implicits._
    def in = Source.single(HttpRequest(uri = Uri.parseAbsolute(indexPage)))
    def getPage = Http().outgoingConnection(hostname)
    def dataBytes = Flow[HttpResponse].map(_.entity.dataBytes).flatten(FlattenStrategy.concat)
    def toUTF8 = Flow[ByteString].map(_.decodeString("utf-8"))
    def parseHTML = Flow[String].map(parseHtml _)
    def getMPLinks = Flow[Node].mapConcat(getLinks _).filter(filterMPLinks _)
    def parseLink = Flow[Node].mapConcat(parseLinkF _)
    def getMPUrl = Flow[MPRequest].map((mp : MPRequest ) => HttpRequest(uri = Uri.parseAbsolute(rootDir ++ mp.source)))
    def getDetails = Flow[Node].mapConcat(getDetailsF _)
    def debug[A] = Flow[A].map { (a : A) => println(a); a }
    def persist  = Flow[MP].map { (mp : MP) => persistMP(mp) ; mp }
    def echo = Sink.foreach(println _)

    in ~> getPage ~> dataBytes ~> toUTF8 ~> parseHTML ~> getMPLinks ~>
      parseLink ~> getMPUrl ~> getPage ~> dataBytes ~> toUTF8 ~> parseHTML ~> getDetails ~> persist ~> echo
  }

  initializeDB
  g.run()

  private def parseHtml(document : String) = {
    val parserFactory = new SAXFactoryImpl
    new NoBindingFactoryAdapter().loadXML(
      new InputSource(new StringReader(document)), parserFactory.newSAXParser()
    )
  }

  private def getLinks(document : Node) = {
    document \\ "a"
  }

  private def filterMPLinks(link : Node) = {
    link.attribute("href").filter { (href) =>
      href.forall { (node) =>
        linkPattern.findFirstIn(node.text).nonEmpty
      }
    }.nonEmpty
  }

  private def parseLinkF(link : Node) = {
    link.attribute("href").toSeq.flatten.map { (href) =>
      MPRequest(link.text.trim, href.text)
    }.to[Seq]
  }

  private def getDetailsF(document : Node) : Seq[MP] = {
    var nameNode = document \\ nameXpath
    val interests = ArticleExtractor.INSTANCE.getText(document.buildString(true));
    Option(nameNode.text).filter(!_.isEmpty).map(MP(_, interests)).to[Seq]
  }

}
