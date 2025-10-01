package com.dc.ui.home

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dc.R
import com.dc.coordinates.LatLon
import com.dc.coordinates.ParobePerimetro
import com.dc.entities.Post
import com.dc.ui.home.maputils.MapMarker
import com.dc.ui.home.maputils.MockPosts
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

class HomeFragment : Fragment() {

    // declare a variable for MapView
    private var mapView: MapView? = null

    private var markers : List<MapMarker> = emptyList()
    private var posts : List<Post> = MockPosts.posts

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

//        Log.d("OsmDroidConfig", "Base Path: ${Configuration.getInstance().osmdroidBasePath}")
//        Log.d("OsmDroidConfig", "Tile Cache Path: ${Configuration.getInstance().osmdroidTileCache}")

        mapView = root.findViewById(R.id.mapViewSimpleLoc)

        markers = mapPostsToMarkers()

        createMap()

        drawPerimeter(ParobePerimetro.coordinates, Color.RED )

        markers.map{ it.draw(mapView!!) }
        mapView!!.invalidate()

        return root
    }

    private fun createMap() {
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setMultiTouchControls(true)
        mapView!!.controller.setZoom(15.00)
    }

    private fun mapPostsToMarkers() : List<MapMarker> {
        return posts.map{ placeMarker(it) }
    }

    private fun placeMarker(post: Post): MapMarker {

        val m = MapMarker(
            post=post,
            onClick = { m ->
                Log.d("CLick", "oi")
                val message = "Clicked on: ${m.post.title}"
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

//        m.draw(mapView!!)

        return m
    }

    private fun drawPerimeter(llpoints: List<LatLon>, themeColor: Int ) {

        val geoPoints = llpoints
            .map { GeoPoint(it.lat, it.lon) }

        val polygon = Polygon().apply {
            points = geoPoints
//            fillPaint.apply {
//                style = Paint.Style.FILL
//                color = themeColor
//                alpha = 15
//            }
            strokeColor = (themeColor)
            strokeWidth = 12f
        }

        mapView!!.overlays.add(polygon)
        mapView!!.invalidate()
    }





}
