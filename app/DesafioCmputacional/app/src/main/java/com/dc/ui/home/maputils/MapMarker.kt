package com.dc.ui.home.maputils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.dc.R
import com.dc.coordinates.CoordinateUtils
import com.dc.coordinates.LatLon
import com.dc.entities.Post
import com.dc.entities.PostType
import com.dc.markers.ForcedSizeDrawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class MapMarker(
    val post: Post,
    val onClick: (MapMarker) -> Unit
) {
    var osmdroidMarker: Marker? = null
        private set

    private val NO_TEXT_THRESHOLD = 15

    fun draw(mapView: MapView) {

//        val centroid = post.points[0]
//        val centroid = CoordinateUtils.getCentroid(post.points)
        val centroid = LatLon(post.latitude, post.longitude)

        if (osmdroidMarker == null) {

            val point = GeoPoint(centroid.lat, centroid.lon)
            val newOsmdroidMarker = Marker(mapView)

            newOsmdroidMarker.position = point
            newOsmdroidMarker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )
            newOsmdroidMarker.title = post.title

            val l = post.body.length
            newOsmdroidMarker.subDescription = post.body.substring(0, minOf(l, 50))


            newOsmdroidMarker.setOnMarkerClickListener { clickedMarker, mapView ->
                this.triggerClick()
                true
            }

            val context = mapView.context
            val icon = when (post.post_type) {
                PostType.FALTA_AGUA -> R.drawable.alert
                PostType.FALTA_LUZ -> R.drawable.alert
                PostType.BURACO -> R.drawable.alert
                else -> R.drawable.construction
            }

            val drawable: Drawable? = ContextCompat.getDrawable(
                context,
               icon
            )

            val lbl = createLabeledBitmap(
                drawable!!,
                post.title,
                mapView.getZoomLevel()
            )
            newOsmdroidMarker.icon = lbl

            this.osmdroidMarker = newOsmdroidMarker

//            if ( this.post.points.size > 1 ) {
//                drawPerimeter(mapView, this.post.points, Color.RED )
//            }

            mapView.overlays.add(this.osmdroidMarker)

            mapView.invalidate() // refresh the map to show the new marker
        }

        mapView.overlays.add(object : Overlay() {
            private var lastZoomLevel = -1.0

            override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
                if (mapView != null) {
                    val currentZoomLevel = mapView.zoomLevelDouble
                    if (currentZoomLevel != lastZoomLevel) {
                        val needsRedraw = (currentZoomLevel < NO_TEXT_THRESHOLD && lastZoomLevel >= NO_TEXT_THRESHOLD) ||
                                (currentZoomLevel >= NO_TEXT_THRESHOLD && lastZoomLevel < NO_TEXT_THRESHOLD)
                        if (needsRedraw) {
                            redrawIcon(mapView)
                        }
                        lastZoomLevel = currentZoomLevel
                    }
                }
            }
        })
    }

    private fun drawPerimeter(mapView: MapView, pts: List<LatLon>, themeColor: Int ) {

        val geoPoints = pts
            .map { GeoPoint(it.lat, it.lon) }

        val polygon = Polygon().apply {
            points = geoPoints
            fillPaint.apply {
                style = Paint.Style.FILL
                color = themeColor
                alpha = 15
            }
            strokeColor = (themeColor)
            strokeWidth = 12f * (mapView.getZoomLevel()/20 )
        }

        mapView.overlays.add(polygon)
        mapView.invalidate()
    }

    private fun redrawIcon(mapView: MapView) {
        osmdroidMarker?.let { marker ->
            val drawable: Drawable? = ContextCompat.getDrawable(mapView.context, R.drawable.alert)
            drawable?.let {
                marker.icon = createLabeledBitmap(it, post.title, mapView.zoomLevel)
                mapView.invalidate()
            }
        }
    }


    fun triggerClick() {
        onClick(this)
    }

    fun createLabeledBitmap(icon: Drawable, label: String, zoomLevel: Int ): ForcedSizeDrawable {
        val dw = 64
        val dh = 64

        val rawBitmap = (icon as BitmapDrawable).bitmap
        val iconBitmap: Bitmap = rawBitmap.scale(dw, dh)

        //  ---------
        var bounds = Rect()
        val paint = Paint().apply {
            color = Color.BLACK;
            textSize = 32f;
            isAntiAlias = true;
        }
        paint.getTextBounds(label, 0, label.length, bounds)
        //  ---------
        var padding = 32
        var gap =16

        if ( zoomLevel < NO_TEXT_THRESHOLD ) {
            gap = 0
            padding = 16
            bounds = Rect(0, 0, 0, 0)
        }
        val y_offset = 32
        val comb_w = padding + dw + gap + bounds.width() + padding
        val comb_h = y_offset + maxOf(dh, bounds.height()) + padding*2

        val combined: Bitmap = createBitmap(comb_w, comb_h)
        val canvas = Canvas(combined)


        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val bgColor = when(post.post_type) {
                PostType.FALTA_AGUA -> Color.rgb(255, 245, 238)
                PostType.FALTA_LUZ -> Color.rgb(255, 236, 217)
                PostType.BURACO -> Color.rgb(255, 224, 192)
                else -> Color.WHITE
            }
            color = bgColor
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val srcRect = Rect(0, 0, iconBitmap.width, iconBitmap.height)
        val destRect = Rect(padding, (comb_h - dh)/2 - y_offset/2, padding+dw, (comb_h + dh)/2 - y_offset/2)

        val paintb = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawRoundRect(
            padding.toFloat()/2,
            padding.toFloat()/2,
            comb_w.toFloat() - padding.toFloat()/2,
            comb_h.toFloat() - padding.toFloat()/2 - y_offset,
            20f, 20f,
            strokePaint
        )

        canvas.drawRoundRect(
            padding.toFloat()/2,
            padding.toFloat()/2,
            comb_w.toFloat() - padding.toFloat()/2,
            comb_h.toFloat() - padding.toFloat()/2 - y_offset,
            20f, 20f,
            bgPaint
        )


        val shadowSpread = 8f
        val radius = 8f
        canvas.drawCircle(
            (comb_w/2).toFloat(),
            (comb_h-y_offset/2).toFloat(),
            radius + shadowSpread,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x60C06000
                style = Paint.Style.FILL
            }
        )

        canvas.drawCircle(
            (comb_w/2).toFloat(),
            (comb_h-y_offset/2).toFloat(),
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x7FC06000
                style = Paint.Style.FILL
            }
        )
        canvas.drawBitmap(iconBitmap, srcRect, destRect, paintb)
        // apenas se está proximo o suficiente
        if ( zoomLevel >= NO_TEXT_THRESHOLD ) {
            canvas.drawText(label, padding.toFloat() + iconBitmap.width + gap, (comb_h + bounds.height()) / 2f - y_offset/2, paint)
        }

        return ForcedSizeDrawable(combined, comb_w, comb_h )
    }



}