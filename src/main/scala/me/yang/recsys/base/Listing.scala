package me.yang.recsys.base

import play.api.libs.json.{JsObject, JsString}
/**
  * Define a Listing of Wikivoyage with necessary info
  * @param pageId
  * @param pageTitle where the listing found
  * @param category "see" or "do"
  * @param name name of place
  * @param alt also known as
  * @param lat latitude
  * @param long longitude
  * @param content description of place
  */
class Listing(pageId:String, pageTitle:String, category:String, name: String, alt:String,
              lat: String, long: String, content: String) {

  def toJSONString: String = {
    val ListingSeq = Seq(
      "pageId" -> JsString(pageId),
      "pageTitle" -> JsString(pageTitle),
      "category" -> JsString(category),
      "name" -> JsString(name),
      "alt" -> JsString(alt),
      "lat" -> JsString(lat),
      "long" -> JsString(long),
      "content" -> JsString(content)
    )
    JsObject(ListingSeq).toString()
  }
}
