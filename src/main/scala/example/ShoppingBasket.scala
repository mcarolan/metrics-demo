package example

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{Gauge, SharedMetricRegistries}
import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}

import scala.collection.mutable.ListBuffer
import scala.io.StdIn

object ShoppingBasket extends App {

  val shoppingBasket = new ListBuffer[String]()

  val graphite = new Graphite(new InetSocketAddress("localhost", 2003))
  val metrics = SharedMetricRegistries.getOrCreate("martin")
  val reporter = GraphiteReporter.forRegistry(metrics).prefixedWith("durrington").build(graphite)

  reporter.start(1, TimeUnit.SECONDS)

  metrics.register("items-in-basket", new Gauge[Int] {
    override def getValue: Int = shoppingBasket.size
  })

  val lengthOfItem = metrics.histogram("item-length")


  def readAndRecord(): Unit = {
    print("Add an item: ")
    val item = StdIn.readLine()

    shoppingBasket += item

    lengthOfItem.update(item.length)

    println(s"Nice. So far you want ${shoppingBasket.mkString(", ")}")

    readAndRecord()
  }

  readAndRecord()
}