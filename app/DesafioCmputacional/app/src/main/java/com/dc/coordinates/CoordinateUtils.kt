package com.dc.coordinates

object CoordinateUtils {

    fun getCentroid(points: List<LatLon>): LatLon {
        if (points.isEmpty()) {
            return LatLon(0.0,0.0)
        }

        var sumLat = 0.0
        var sumLon = 0.0

        for (point in points) {
            sumLat += point.lat
            sumLon += point.lon
        }

        val count = points.size
        return LatLon(sumLat / count, sumLon / count)
    }
}