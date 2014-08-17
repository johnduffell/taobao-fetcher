import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import play.api.libs.ws._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scalax.io._
import scalax.io.Resource

object Fetcher {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  val driver = new ChromeDriver()

  //http://www.tesco.com/groceries/product/search/default.aspx?searchBox=cow+gate+900g&newSort=true&search=Search

  def main(args: Array[String]) = {
    driver.get("http://www.tesco.com/groceries/product/search/default.aspx?searchBox=cow+gate+900g")

    val products = driver.findElements(By.xpath("//*[@class='productLists']//li")).asScala.map(_.getAttribute("data-product-id")).toStream

    val data = products.map(getProduct)

    data.foreach({
      case Product(id, image, title, url, description, originalPrice, cost) =>

        val imageFile: Output = Resource.fromFile(s"output/$id.jpg")
        imageFile.write(image)

        val descriptionFile: Output = Resource.fromFile(s"output/$id.html")
        descriptionFile.write(s"""<h1>$title</h1>\n\n<p>$originalPrice</p>\n\n<p>$cost</p>\n\n<a href="$url">tesco</a><div>$description</div>""")

    })

    driver.quit()

  }

  case class Product(id: String, image: List[Byte], title: String, url: String, desc: String, originalPrice: String, cost: Int)

  val profit = 2500
  val conversion = 10.5
  val postage = 2900
  val packaging = 1100
  val units = 6

  def getProduct(id: String) = {
    val url = s"http://www.tesco.com/groceries/product/details/?id=$id"
    driver.get(url)


    val descriptionDetails = driver.findElement(By.className("descriptionDetails"))

    val originalPrice = descriptionDetails.findElement(By.className("linePrice")).getText
    val penceString = """£([0-9])+\.([0-9][0-9])""".r.replaceFirstIn(originalPrice, "$1$2")
    val pence = Integer.parseInt(penceString)
    val totalFor6 = conversion * ((pence * units) + postage + packaging + profit) / 100
    val rounded = (Math.round(totalFor6/100)*100).toInt

    println(s"price: $pence")

    val title = descriptionDetails.findElement(By.xpath(".//h1")).getText
    println(s"title: $title")


    val imageUrl = driver.findElement(By.className("productImage")).findElement(By.xpath("./img")).getAttribute("src")
    println(s"image url: $imageUrl")
    val imageBytes = Await.result(WS.url(imageUrl).get(), 10.seconds).ahcResponse.getResponseBodyAsBytes.toList

    val productDetails = driver.findElement(By.className("productDetails"))

    val h2sDivs = productDetails.findElements(By.xpath("//*[@class='detailsWrapper']/*")).asScala.toList

    val description = h2sDivs.map(element =>
    if (element.getTagName == "h2") "<h2>" + element.getText + "</h2>"
    else s"<p>${element.getText}</p>")

    val paragraphs = description.mkString("\n")

    println(s"description: $paragraphs")



    Product(id, imageBytes, title, url, paragraphs, originalPrice, rounded)
  }

}
