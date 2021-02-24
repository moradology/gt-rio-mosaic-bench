#!/usr/bin/env python3
import sys
from time import time
from typing import Dict

import rasterio
from fastapi import FastAPI, Response
from rio_tiler.io import COGReader
from rio_tiler.mosaic import mosaic_reader
from rio_tiler.models import ImageData
from titiler.ressources.enums import ImageType

from morecantile.defaults import tms as defaultTileMatrices
from morecantile.models import TileMatrixSet
from starlette.middleware.cors import CORSMiddleware


args = sys.argv
z = int(args[1])
x = int(args[2])
y = int(args[3])

def _reader(asset: str, x: int, y: int, z: int) -> ImageData:
    with COGReader(asset) as src_dst:
        return src_dst.tile(x, y, z, indexes=[1,2,3])

# warmup gdal
mosaic_assets_warmup = [
    "https://naipeuwest.blob.core.windows.net/naip/v002/pa/2013/pa_100cm_2013/39077/m_3907709_ne_18_1_20130906.tif",
    "https://naipeuwest.blob.core.windows.net/naip/v002/pa/2013/pa_100cm_2013/39077/m_3907709_nw_18_1_20130906.tif",
    "https://naipeuwest.blob.core.windows.net/naip/v002/pa/2013/pa_100cm_2013/39077/m_3907710_nw_18_1_20130906.tif"
]
data_warmup, _ = mosaic_reader(mosaic_assets_warmup, _reader, 1161, 1552, 12)
image_warmup = data_warmup.post_process(
    in_range=None,
    color_formula=None,
)
format = ImageType.png
image_warmup.render(
    add_mask=True,
    img_format=format,
    colormap=None,
    **format.profile,
)
# warmup end


start = time()
mosaic_assets = [
    "https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807628_nw_18_1_20130912.tif",
    "https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807627_ne_18_1_20130912.tif",
    "https://naipeuwest.blob.core.windows.net/naip/v002/md/2013/md_100cm_2013/38076/m_3807628_ne_18_1_20130915.tif"
]
data, _ = mosaic_reader(mosaic_assets, _reader, x, y, z)
image = data.post_process(
    in_range=None,
    color_formula=None,
)
format = ImageType.png
rendered = image.render(
    add_mask=True,
    img_format=format,
    colormap=None,
    **format.profile,
)
print(time() - start)