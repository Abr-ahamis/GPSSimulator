package com.gpssimulator.utils

import com.gpssimulator.data.model.LocationPoint

object PolylineDecoder {
    fun decode(encodedPath: String): List<LocationPoint> {
        val len = encodedPath.length
        val path = mutableListOf<LocationPoint>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            
            result = 1
            shift = 0
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            
            path.add(LocationPoint(latitude = lat * 1e-5, longitude = lng * 1e-5))
        }
        return path
    }
}
