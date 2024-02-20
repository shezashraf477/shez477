package com.nasdaq.ktable.updater.demo

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import io.circe.parser.decode
import io.circe.generic.auto._
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util
import java.util.Map

@JsonIgnoreProperties(ignoreUnknown = true)
case class Alert2(
                   @JsonProperty("prefID") prefId: Int,
                   @JsonProperty("destinationType") destinationType: String,
                   @JsonProperty("ruleTxt") ruleTxt: String
                 )
object Main extends App {

  import com.fasterxml.jackson.databind.ObjectMapper

  val jsonString =
    """
      |{
      |  "alert2": {
      |    "prefID": 12345,
      |    "destinationType": "EMAIL",
      |    "ruleTxt": "(\"39\" in data and data[\"39\"]==\"0\") and (\"150\" in data and data[\"150\"]==\"0\") and (\"20\" in data and data[\"20\"]==\"0\") and (\"143\" in data and data[\"143\"] == \"LN\")"
      |  }
      |}
      |""".stripMargin

  // Initialize Jackson ObjectMapper with Scala module
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  val jsonObject: util.Map[_, _] = mapper.readValue(jsonString, classOf[util.Map[_, _]])
  val order = mapper.convertValue(jsonObject.get("order"), classOf[Order])
  val alert = mapper.convertValue(jsonObject.get("alert2"), classOf[Alert])
  System.out.println("whre we go " + alert)


  // Parse JSON string to Alert2 object
  val alert2: Alert2 = mapper.readValue(jsonString, classOf[Alert2])
  // Print the Alert2 object
  println(alert2)
}
