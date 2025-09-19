package com.dc.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dc.R
import com.dc.coordinates.ParobePerimetro
import com.dc.markers.ForcedSizeDrawable
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import java.io.File

class HomeFragment : Fragment() {

    // declare a variable for MapView
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textView: TextView = root.findViewById(R.id.text_home)

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val ctx = requireContext(   )
        val act = requireActivity( )

        Log.d("OsmDroidConfig", "Base Path: ${Configuration.getInstance().osmdroidBasePath}")
        Log.d("OsmDroidConfig", "Tile Cache Path: ${Configuration.getInstance().osmdroidTileCache}")

        mapView = root.findViewById(R.id.mapViewSimpleLoc)

        createMap()
        cityPerimeter()
        placeMarker()

//        mapView!!.invalidate()d
        return root
    }

    private fun createMap() {
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setMultiTouchControls(true)
        mapView!!.controller.setZoom(15.00)
    }
    private fun placeMarker() {

        val parobe = GeoPoint(-29.62662577489841, -50.82894312606729)

        mapView!!.controller.setZoom(20.0)
        mapView!!.controller.setCenter(parobe)

        val marker = Marker(mapView)


        // Load your PNG from drawable
        val ctx = requireContext()
        val drawable: Drawable? = ContextCompat.getDrawable(ctx, R.drawable.construction)

        val lbl = createLabeledBitmap(
            drawable!!,
            "Construção Na prefeitura"
        )

        marker.icon = lbl

        marker.title = "Alguma construção rolando aqui"
        marker.subDescription= "Algum conteúdo que possa ser relevante"

        marker.position = parobe
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


        marker.setOnMarkerClickListener { clickedMarker, mapView ->
            Toast.makeText(mapView.context, "Marker clicked: ${clickedMarker.title}", Toast.LENGTH_SHORT).show()
            true
        }

        mapView!!.overlays.add(marker)

        // Overlay to draw the hitbox
        val hitboxOverlay = object : Overlay() {
            override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
                super.draw(canvas, mapView, shadow)

                // Get the screen coordinates of the marker position
                val point = mapView.projection.toPixels(marker.position, null)

                // Calculate the rectangle around the icon
                val iconWidth = marker.icon.intrinsicWidth
                val iconHeight = marker.icon.intrinsicHeight

                val left = point.x - iconWidth / 2
                val top = point.y - iconHeight
                val right = point.x + iconWidth / 2
                val bottom = point.y

                // Draw the red rectangle
                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }

        mapView!!.overlays.add(hitboxOverlay)
        mapView!!.invalidate()
    }


    private fun cityPerimeter() {
        // converte para geopoints
        val geoPoints = ParobePerimetro
            .coordinates.
            map { GeoPoint(it.lat, it.lon) }

        // cria um polígono
        val polygon = Polygon().apply {
            points = geoPoints
            fillColor = 0xFF0000 // semi-transparent red
            strokeColor = 0xFFFF0000.toInt() // solid red border
            strokeWidth = 4f
        }

        // adiciona polygon ao mapa
        mapView!!.overlays.add(polygon)
        mapView!!.invalidate()
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
