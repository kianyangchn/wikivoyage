package me.yang.recsys.utils

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import me.yang.recsys.base.Listing

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.XML
import scala.xml.pull._

class WikivoyageParser() {
  val categories = Array("see", "do") // care about listings of "see" and "do" categories
  val targetFeatures = Array("name", "alt", "lat", "long", "content") // necessary features to be extracted from a listing

  val AllListings = ArrayBuffer[Listing]()

  /**
    * parse out pages from raw xml
    * keep tracking pages and "see"/"do" categories in pages
    */
  def parseXml(xmlFile: String): Unit = {
    // read raw xml file as xml events
    val docs = new XMLEventReader(Source.fromFile(xmlFile))
    var pageFound = false
    var page = ArrayBuffer[String]() // all text between one page-tag-pair
    for (event <- docs) {
      event match {
        // page start found
        case EvElemStart(_, "page", _, _) =>
          pageFound = true
          val tag = "<page>"
          page += tag
        // page end found -> extract listings of "see" and "do"
        case EvElemEnd(_, "page") =>
          val tag = "</page>"
          page += tag
          pageFound = false
          parseListings(page)
          page.clear()
        // tag start inside a page found
        case _@EvElemStart(_, tag, _, _) =>
          if (pageFound) {
            page += ("<" + tag + ">")
          }
        // tag end inside a page found
        case _@EvElemEnd(_, tag) =>
          if (pageFound) {
            page += ("</" + tag + ">")
          }
        // keep all other text inside a page
        case EvText(text) =>
          if (pageFound) {
            page += text
          }
        case _ => // ignore
      }
    }
  }

  /**
    * Extract necessary information from a single page
    * id, title, text
    * then wrap them into Listing objects
    * @param page
    */
  private def parseListings(page: ArrayBuffer[String]): Unit = {
    val pageElem = XML.loadString(page.mkString)
    val pageId = (pageElem \ "id").text
    val pageTitle = (pageElem \ "title").text
    val pageText = (pageElem \ "revision" \ "text").text // listings of the page are in text
    wrapperListings(pageText, pageId, pageTitle)
  }

  /**
    * Extract all listings from text of page
    * @param text
    * @param pageId
    * @param pageTitle
    */
  private def wrapperListings(text: String, pageId: String, pageTitle: String): Unit = {
    for (category <- categories) {
      // listing block should start with "{{see" or "{{do"
      val categorySignal = "{{" + category
      // try to find the start and end index of next listing block
      var st = text.indexOf(categorySignal)
      while (st >= 0) { // if st<0 then no more listing of target category
        var ed = st + categorySignal.length
        // use stack to track bracket
        // a block is finished only if the number of { and } is the same
        var stack = 2
        while (stack > 0 && ed < text.length) {
          if (text(ed) == '}') stack -= 1
          else if (text(ed) == '{') stack += 1 // situation: content may have brackets
          ed += 1
        }
        if (stack == 0) {
          val listingText = text.substring(st, ed - 2)
          // a listing block may have many lines, split them with \n
          // split each line by | to separate features
          // flatMap to build a 1-dim array
          val flatListing = listingText.split("\n")
            .flatMap(_.trim.split("[|]").filter(_.length > 0))
          // split feature by = to get key-value pair
          val features = flatListing
            .map(_.trim.split("="))
            .filter(_.length>=1)
            .map(feature => {
              if (feature.length == 1) (feature(0), "") // no value
              else if (feature.length == 2) (feature(0), feature(1)) // simple key and value
              else (feature(0), feature.drop(1).mkString) // value could have = as well, so combination necessary
            }).toMap
          // only keep key-value pairs for target features
          val caredFeatures = targetFeatures.map(feature => {
            if (features.contains(feature)) (feature, features(feature))
            else (feature, "")
          }).toMap
          // wrap listing features with pageId and pageTitle to Listing object and append to AllListings
          AllListings.append(new Listing(pageId, pageTitle, category,
            caredFeatures("name"), caredFeatures("alt"), caredFeatures("lat"), caredFeatures("long"), caredFeatures("content")))
        }
        // move st to start index of next listing block
        st = text.indexOf(categorySignal, ed)
      }
    }
  }

  def save(localFile:String): Unit = {
    val write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localFile)))
    for (listing <- AllListings) {
      write.write(listing.toJSONString + "\n")
    }
    write.close()
  }
}
