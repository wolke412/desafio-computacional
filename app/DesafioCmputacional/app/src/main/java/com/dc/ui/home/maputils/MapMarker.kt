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
import com.dc.markers.ForcedSizeDrawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MapMarker(
    val post: Post, // Property to hold the Post object
    val onClick: (MapMarker) -> Unit // Callback function for click events
) {
    var osmdroidMarker: Marker? = null
        private set

    init {  }

    fun draw(mapView: MapView) {

//        val centroid = post.points[0]
        val centroid = CoordinateUtils.getCentroid(post.points)
        if (osmdroidMarker == null) {

            val point = GeoPoint(centroid.lat, centroid.lon)
            val newOsmdroidMarker = Marker(mapView)

            mapView.controller.setZoom(20.0)
            mapView.controller.setCenter(point)

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
            val drawable: Drawable? = ContextCompat.getDrawable(
                context,
               R.drawable.construction
            )
            val lbl = createLabeledBitmap(
                drawable!!,
                post.title
            )
            newOsmdroidMarker.icon = lbl

            this.osmdroidMarker = newOsmdroidMarker


            if ( this.post.points.size > 1 ) {
                drawPerimeter(mapView, this.post.points, Color.RED )
            }

            mapView.overlays.add(this.osmdroidMarker)

            mapView.invalidate() // Refresh the map to show the new marker
        }
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


    fun triggerClick() {
        onClick(this)
    }

    fun createLabeledBitmap(icon: Drawable, label: String): ForcedSizeDrawable {
        val dw = 64
        val dh = 64

        val rawBitmap = (icon as BitmapDrawable).bitmap
        val iconBitmap: Bitmap = Bitmap.createScaledBitmap(
            rawBitmap,
            dw,
            dh,
            true
        )

        val drawbleIcon = ForcedSizeDrawable(iconBitmap, dw, dh)

        val paint = Paint().apply {
            color = Color.BLACK;
            textSize = 32f;
            isAntiAlias = true;
        }
        val bounds = Rect()

        paint.getTextBounds(label, 0, label.length, bounds)

        val comb_w = dw + bounds.width() + 10
        val comb_h = maxOf(dh, bounds.height())

        val combined = Bitmap.createBitmap(
            comb_w,
            comb_h,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combined)

        // Draw background rectangle behind icon + text
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color= Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth= 60f

        }

        val srcRect = Rect(0, 0, iconBitmap.width, iconBitmap.height)
        val destRect = Rect(0, (comb_h - dh)/2, dw, dh)
        // scale to 64x64 and center vertically

        val paintb = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawRect(
            0f,
            0f,
            comb_w.toFloat(),
            comb_h.toFloat(),
            bgPaint
        )
        canvas.drawBitmap(iconBitmap, srcRect, destRect, paintb)

        canvas.drawText(label, iconBitmap.width + 5f, iconBitmap.height / 2f + bounds.height()/2f, paint)

        return ForcedSizeDrawable(combined, comb_w, comb_h )
    }



}