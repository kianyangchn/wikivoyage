package me.yang.recsys

import me.yang.recsys.utils.WikivoyageParser

object Boot {
  def main(args: Array[String]): Unit = {
    val xmlFile = args(0)
    val localFile = args(1)
    val wikivoyageParser = new WikivoyageParser()
    wikivoyageParser.parseXml(xmlFile)
    wikivoyageParser.save(localFile)
  }
}
