package com.dc.ui.home

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dc.R
import com.dc.api.ApiWrapper
import com.dc.api.getContent
import com.dc.api.getError
import com.dc.api.isSuccess
import com.dc.coordinates.LatLon
import com.dc.coordinates.ParobePerimetro
import com.dc.entities.Post
import com.dc.ui.home.maputils.MapMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

class HomeFragment : Fragment() {

    // declare a variable for MapView
    private var mapView: MapView? = null

    private var markers : List<MapMarker> = emptyList()
    private var posts : List<Post> = emptyList()

    private fun fetchPosts() {

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Carregando posts...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch (Dispatchers.IO) {
            val response = ApiWrapper.fetchPreviewPosts()

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()

                if (response.isSuccess ) {
                    val p = response.getContent()
                    if ( p != null ) {
                        posts = p
                        Log.d("ApiError", "Got posts: " + p.size)
                        setMarkers()
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Falha ao carregar posts : " + response.getError()?.error,
                        Toast.LENGTH_LONG
                    ).show()
                    
                    Log.e("ApiError", response.getError()?.error ?: " unknown")

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fetchPosts()

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textView: TextView = root.findViewById(R.id.text_home)
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

//        Log.d("OsmDroidConfig", "Base Path: ${Configuration.getInstance().osmdroidBasePath}")
//        Log.d("OsmDroidConfig", "Tile Cache Path: ${Configuration.getInstance().osmdroidTileCache}")

        mapView = root.findViewById(R.id.mapViewSimpleLoc)

        createMap()

        drawPerimeter(ParobePerimetro.coordinates, Color.RED )

        mapView!!.invalidate()

        return root
    }

    private fun setMarkers() {
        markers = mapPostsToMarkers()
        markers.map{ it.draw(mapView!!) }
    }

    private fun createMap() {
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setMultiTouchControls(true)
        mapView!!.controller.setZoom(14.00)
        val startPoint = GeoPoint( -29.6383, -50.8276)
        mapView!!.controller.setCenter(startPoint)
    }

    private fun mapPostsToMarkers() : List<MapMarker> {
        return posts.map{ placeMarker(it) }
    }

    private fun placeMarker(post: Post): MapMarker {

        val m = MapMarker(
            post=post,
            onClick = { m ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val response = ApiWrapper.getPostById(m.post.id_post)
                    withContext(Dispatchers.Main) {
                        if(response.isSuccess) {
                            response.getContent()?.let { post ->
                                Log.d("ApiSuccess", "Got post: " + post.title)
                                val postSheet = PostSheet(post)
                                postSheet.show(childFragmentManager, "PostSheet")
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Falha ao carregar post: " + response.getError()?.error,
                                Toast.LENGTH_LONG
                            ).show()

                            Log.e("ApiError", response.getError()?.error ?: " unknown")
                        }
                    }
                }
//                val postSheet = PostSheet(m.post)
//                postSheet.show(childFragmentManager, "PostSheet")
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
