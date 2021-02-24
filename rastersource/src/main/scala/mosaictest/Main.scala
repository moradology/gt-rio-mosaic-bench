package com.azavea.mosaictest

import geotrellis.vector.Extent
import geotrellis.raster.{RasterSource, MosaicRasterSource}
import geotrellis.proj4.WebMercator
import geotrellis.layer._

import cats.data.{NonEmptyList => NEL}
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.http4s.implicits._

import scala.concurrent.duration._

object Main {
  def main(args: Array[String]) = {
    try {
      val z = args(0).toInt
      val x = args(1).toInt
      val y = args(2).toInt
      val tmsLevels: Array[LayoutDefinition] = {
        val scheme = ZoomedLayoutScheme(WebMercator, 256)
        for (zoom <- 0 to 64) yield scheme.levelForZoom(zoom).layout
      }.toArray

      val t0 = System.currentTimeMillis()
      val rasterSources = NEL.of(
          RasterSource("gtiff+https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807628_nw_18_1_20130912.tif"),
          RasterSource("gtiff+https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807627_ne_18_1_20130912.tif"),
          RasterSource("gtiff+https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807628_ne_18_1_20130915.tif")
      )
      val mosaicSource = MosaicRasterSource(rasterSources, WebMercator)

      val layout = tmsLevels(z)
      val key    = SpatialKey(x, y)
      val layedOut = mosaicSource.tileToLayout(layout)
      val raster = layedOut.read(key, List(0, 1, 2)).getOrElse(throw new Exception("failed to retrieve tile"))
      val rendered = raster.color.renderPng
      val bytes = rendered.bytes
      val t1 = System.currentTimeMillis()
      println((t1 - t0).toDouble / 1000.0)

      val bos = new java.io.BufferedOutputStream(new java.io.FileOutputStream("test.png"))
      bos.write(bytes)

    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }
  }
}
