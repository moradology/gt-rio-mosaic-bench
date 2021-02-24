# TNC Geometry Merger and Statistics Collection


### Objectives

The goal of this code is to take a great many geometries and calculate what is sometimes
referred to as the union of them all as an intermediate step in calculating statistics
on a handful of large datasets.
The output of this process is all intersecting regions across all datasets as well as
each region lacking interesction.
In the simple case of two partially overlapping circles, we would expect two polygons
representing the non-intersecting bits of each circle as well as a polygonal region which
represents the overlapping bits.

The purpose of this data is to serve as an intermediate product for calculations of land use
across the multidimensional dataset consisting of *all* regions covered by any of the underlying
sets.
On top of the input datasets' various attributes, statistics regarding land use over each polygon
are calculated from land use datasets in 2010 and 2017 and a canopy change dataset.
These statistics are added to the final merger's attribute table.
By processing these attributes and statistics in batch, and providing the appropriate indices
much of the time consuming spatial processing which would otherwise be necessary when asking
simple questions about said multidimensional dataset can be avoided.


### Code Divisions

This source includes three different `main` classes which jointly aim to build a database
consisting of the full geometric mashup and all coincident attributes from input datasets.
Two of the classes, [GeojsonCanopyIngest](src/main/scala/tncmerge/ingest/GeojsonCanopyIngest.scala)
and [GeojsonIngest](src/main/scala/tncmerge/ingest/GeojsonIngest.scala), provide a relatively quick
ingest of *massive* (tens of gigabytes) geojson files which contain land use and canopy change
metrics.
The class which actually runs the merger and attribute correlation is
[Merge](src/main/scala/tncmerge/Merge.scala).


### Running Things

A brief note about the lack of abstraction in this code is in order.
There are a number of fields which could well be provided as arguments at runtime.
Any future work extending this code should consider altering fields to allow runtime arguments
for things such as table names as this will ease code use in environments which might be slightly
different from those in which this code was developed.
As is, special attention should be paid to the table names, column mappings, and even configuration
for the [hikari pool](https://www.baeldung.com/hikaricp) as these values might need to be tuned
in future applications running in other computational contexts.


#### Hardware Used

Both the database and code were run on an AWS `c5ad.16xlarge` which provides 64 processors.
This code could, owing to the high degree of parallelism, be scaled up or down to a different
number of processors but it is advisable to use a colocated SSD for fast databse
access as it is primarily database connections which serve as a bottleneck and disk access speed
is near certain to make a difference in query speed.


#### Ingest Geojson

The geojson ingest simply consumes a geojson file and inserts a row per geometry in the provided
`FeatureCollection`.
`GeojsonIngest` takes a `--path` and a `--table` parameter which specify the geojson file location
and the table in which geometries are to be inserted, respectively.
This process is currently hardcoded to 2010 and 2017 landcover datasets explicitly and will require
some minor alterations to be run on different years.
`GeojsonCanopyIngest` takes a `--path` and a `--table` parameter as well but is not obviously
specific to a given analysis year and might work with no alterations on future versions/different
years of the canopy change dataset.


#### Merge Geometries, Correlate Attributes, Calculate Stats

This project's reason for being is captured in the `Merge` main function mentioned above.
It serves as the final (and largest) step which can be kicked off as soon as all dependent database
tables are fully populated.
To calculate a geometry merger on the scale of the city of New York with all of the data we have
required some degree of problem restatement.
Instead of calculating for the entirety of NYC, work is broken down into a number of columns and
rows which serve as units of processing and parallel computation.

Briefly, the steps taken once we've decided on a column/row are:
1. calculate the geometric window/bounding box/extent of said column/row
2. query all tables to grab all intersections with the window from 1
3. decompose these many partially overlapping polygons into their constituent rings (inner and outer)
4. polygonize all regions between these linestrings
5. find a point on the surface of each polygon and query the input geometries for intersection and, therefore, coincident attributes
6. generate the intersection of all land use/canopy change polygons, calculate sq ft of overlap and associate these calculations
7. bulk insert records (the finished product) to a new table

Steps 3 and 4 are well understood methods to find all the unique regions of overlap/non-overlap
within a set of polygons.
[Here](http://blog.cleverelephant.ca/2019/07/postgis-overlays.html), Paul Ramsey discusses steps
3, 4, and 5 in the context of PostGIS alone.
This exact strategy proved too slow for the scale of data processed in this effort and too prone to
topological errors which could only be avoided by using an unreleased version of
[JTS](https://github.com/locationtech/jts) (which is why this project was written to run on the JVM).
Specifically, it was the addition of
[`OverlayNG` merger strategies](http://lin-ear-th-inking.blogspot.com/2020/06/jts-overlayng-tolerant-topology.html)
which made the vast overlays in this project possible.
