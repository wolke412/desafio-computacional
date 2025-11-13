package com.dc.ui.posts

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.fragment.app.Fragment
import com.dc.R
import com.dc.api.ApiWrapper
import com.dc.api.getContent
import com.dc.api.getError
import com.dc.api.isError
import com.dc.api.isSuccess
import com.dc.coordinates.LatLon
import com.dc.coordinates.ParobePerimetro
import com.dc.entities.E_CreatePost
import com.dc.entities.PostType
import com.dc.entities.toCreatePostBody
import com.dc.utils.SessionManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import java.io.File

class CreatePostFragment : Fragment() {


    private lateinit var imageViewPreview: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var radioGroupTags: RadioGroup
    private lateinit var selectedTag: PostType

    private var capturedImage: Bitmap? = null
    private lateinit var mapView: MapView
    private var selectedLocation: GeoPoint? = null
    private lateinit var myLocationProvider: GpsMyLocationProvider


    // ActivityResultLauncher for the camera
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                capturedImage = imageBitmap
                imageViewPreview.setImageBitmap(imageBitmap)
                imageViewPreview.visibility = View.VISIBLE
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize osmdroid configuration
        val osmConfig = Configuration.getInstance()
        osmConfig.load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val cacheDir = requireContext().getExternalFilesDir(null)

        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache= File(cacheDir, "tiles")
        osmConfig.userAgentValue = requireContext().packageName // Set User Agent
        myLocationProvider = GpsMyLocationProvider(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the action bar for this fragment
        // (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Initialize views
        imageViewPreview = view.findViewById(R.id.imageView_preview)
        titleEditText = view.findViewById(R.id.editText_title)
        bodyEditText = view.findViewById(R.id.editText_body)
        radioGroupTags = view.findViewById(R.id.radioGroup_tags)

        populateTagsRadioGroup()

        val takePictureButton: Button = view.findViewById(R.id.button_take_picture)
        val submitPostButton: Button = view.findViewById(R.id.button_submit_post)

        mapView = view.findViewById(R.id.map)




        setupMap()
        requestLocationPermission()

        takePictureButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // usa camera de tras
            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 0)
            takePictureLauncher.launch(takePictureIntent)
        }

        submitPostButton.setOnClickListener {
            val postEntity = createPostEntityFromForm()
            if (postEntity == null) {
                return@setOnClickListener
            }


            Toast.makeText(
                requireContext(),
                "Enviando requisição...", Toast.LENGTH_LONG
            ).show()

            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("Enviando postagem...")
                setCancelable(false)
                show()
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val response = ApiWrapper.createPost(postEntity.toCreatePostBody())
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    if (response.isSuccess ) {
                        val content = response.getContent()
                        val postId = content?.id_post!!

                        if ( postEntity.image != null ) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val imageres = ApiWrapper.uploadPostImage(postId, postEntity.image )
                                if (imageres.isError) {
                                    Log.e("CreatePostFragment", "Error uploading image: ${imageres.getError()?.error}")
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Postagem criada com sucesso!", Toast.LENGTH_LONG).show()
                                    parentFragmentManager.popBackStack()
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Postagem criada com sucesso!", Toast.LENGTH_LONG).show()
                            parentFragmentManager.popBackStack()
                        }
                        Toast.makeText(requireContext(), "Postagem criada com sucesso!", Toast.LENGTH_LONG).show()
                        // parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Falha ao criar postagem: " + response.getError()?.error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }



            }
        }
    }

    private fun populateTagsRadioGroup() {
        val tags = listOf("Buraco", "Falta de água", "Falta de Luz")
        val postTypes = listOf(
        PostType.BURACO, PostType.FALTA_AGUA, PostType.FALTA_LUZ)

        tags.forEachIndexed { index, tagName ->
            val radioButton = RadioButton(context).apply {
                text = tagName
                id = View.generateViewId()
                tag = postTypes[index]
            }

            radioButton.setOnClickListener {
                selectedTag = it.tag as PostType
            }

            if (index == 0) {
                radioButton.isChecked = true
                selectedTag = postTypes[index]
            }

            radioGroupTags.addView(radioButton)
        }
    }
    private fun createPostEntityFromForm(): E_CreatePost? {
        val title = titleEditText.text.toString()
        val body = bodyEditText.text.toString()
        val image = capturedImage
        val location = selectedLocation

        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return null
        }

        if (location == null) {
            Toast.makeText(requireContext(), "Please select a location on the map", Toast.LENGTH_SHORT).show()
            return null
        }

        // sets test user
        val userId = SessionManager.getInstance(requireContext()).getUserId()

        if (userId == -1) {
            Toast.makeText(requireContext(), "Invalid session", Toast.LENGTH_SHORT).show()
            return null
        }

        return E_CreatePost(
            userId = userId,
            title = title,
            body = body,
            latitude = location.latitude,
            longitude = location.longitude,
            image = image,
            type = (selectedTag)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMap() {

        mapView.controller.setZoom(18.0)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    addMarker(it)
                    selectedLocation = it
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        mapView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                //
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
//           false
        }

        drawPerimeter(ParobePerimetro.coordinates, Color.RED)

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add( mapEventsOverlay)
    }

    private fun drawPerimeter(llpoints: List<LatLon>, themeColor: Int ) {
        val geoPoints = llpoints
            .map { GeoPoint(it.lat, it.lon) }

        val polygon = Polygon().apply {
            points = geoPoints
            strokeColor = (themeColor)
            strokeWidth = 12f
        }

        mapView!!.overlays.add(polygon)
        mapView!!.invalidate()
    }


    private fun addMarker(geoPoint: GeoPoint) {
        // Clear previous markers
        mapView.overlays.removeAll { it is Marker }

        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh the map
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                getCurrentLocation()
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        myLocationProvider.startLocationProvider(object : IMyLocationConsumer {
            override fun onLocationChanged(location: android.location.Location?, source: IMyLocationProvider?) {
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    selectedLocation = userLocation
                    mapView.controller.setCenter(userLocation)
                    addMarker(userLocation)

                    // We only need the first location fix to center the map
                    myLocationProvider.stopLocationProvider()
                    Toast.makeText(requireContext(), "Tap on the map to change the location", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(requireContext(), "Could not get current location", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        // Stop location provider to save battery
        myLocationProvider.stopLocationProvider()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Show the action bar again when the fragment is destroyed
        // (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
}

