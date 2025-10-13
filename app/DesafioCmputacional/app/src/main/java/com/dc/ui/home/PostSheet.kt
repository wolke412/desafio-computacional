package com.dc.ui.home

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.graphics.Color
import android.os.Build
import android.widget.Button
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import coil.imageLoader
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.load
import com.dc.R
import coil.util.DebugLogger
import com.dc.api.ApiWrapper
import com.dc.components.Drawer
import com.dc.entities.Post
import com.dc.coordinates.LatLon
import okhttp3.OkHttpClient

class PostSheet(private var post: Post) : Drawer() {
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var imageContainer: LinearLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val m = super.initModal()

        createTitle()
        createDescription()
        createImageContainer()
        createAddress()

        createActionButtons()
        populateImages()

        return m
    }

    private fun createTitle() {
        titleView = TextView(requireContext())
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,0,0,32)
        titleView.layoutParams = params
        titleView.text = post.title

        titleView.textSize=24f
        titleView.setTextColor(Color.BLACK)
        mainLayout.addView(titleView)
    }

    private fun createDescription() {
        descriptionView = TextView(requireContext())
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,0,0,32)
        descriptionView.layoutParams = params
        descriptionView.text = post.body
        descriptionView.textSize = 16f
        mainLayout.addView(descriptionView)

    }

    private fun createAddress() {
        val addressContainer = LinearLayout(requireContext())
        addressContainer.orientation = LinearLayout.HORIZONTAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,0,0,16)
        addressContainer.layoutParams = params

        val icon = ImageView(requireContext())
        val drawable = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_myplaces)
        drawable?.setTint(Color.BLACK)
        icon.setImageDrawable(drawable)
        addressContainer.addView(icon)

        val addressView = TextView(requireContext())
        addressView.setTextColor(Color.BLACK)
        addressView.text = "Buscando endereço..."

        Log.d("PostSheet", "Finding address for " + post.latitude +", " + post.longitude)
        val latlon = LatLon(post.latitude, post.longitude)

        latlon.getAddress(requireContext(), post.latitude, post.longitude) { address ->
            Log.d("PostSheet", "Got address: " + address)
            addressView.text = address
        }

        addressContainer.addView(addressView)
        mainLayout.addView(addressContainer)
    }

    private fun createImageContainer() {
        imageContainer = LinearLayout(requireContext())
        imageContainer.orientation = LinearLayout.VERTICAL

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        params.setMargins(0,0,0, 32)

        imageContainer.layoutParams = params
        mainLayout.addView(imageContainer)
    }

    private fun populateImages() {
        val urls = post.post_images

        if (urls == null) {
            Log.d("PostSheet" , "No images to load for this post." )
            return
        }

        Log.d("PostSheet" ,  "loading images..." )

        for (url in urls) {
            val imgView = ImageView(requireContext())
            imgView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imgView.adjustViewBounds = true

            var imageUrl = ApiWrapper.HOSTNAME + url
            Log.d("PostSheet" , imageUrl )


            val okHttpClient = OkHttpClient.Builder().build()

            val imageLoader = ImageLoader.Builder(requireContext())
                .okHttpClient(okHttpClient)
                .logger(DebugLogger())
                .build()

            imgView.load(imageUrl, imageLoader) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_close_clear_cancel)
            }
            imageContainer.addView(imgView)
        }

        Log.d("PostSheet" , "" + urls.size + " images loaded." )
    }

    private fun createActionButtons() {
        val buttonContainer = LinearLayout(requireContext())
        buttonContainer.orientation = LinearLayout.HORIZONTAL
        buttonContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val usefulButton = Button(requireContext())
        usefulButton.text = "Achei útil"
        val usefulDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_arrow_up)
        usefulDrawable?.setTint(0x44BB55)
        usefulButton.setCompoundDrawablesWithIntrinsicBounds(usefulDrawable, null, null, null)
        usefulButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val irrelevantButton = Button(requireContext())
        irrelevantButton.text = "Achei irrelevante"
        val irrelevantDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_arrow_down)
        irrelevantDrawable?.setTint( 0xFF4455 )
        irrelevantButton.setCompoundDrawablesWithIntrinsicBounds(
            irrelevantDrawable,
            null,
            null,
            null
        )


        irrelevantButton.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        buttonContainer.addView(usefulButton)
        buttonContainer.addView(irrelevantButton)

        mainLayout.addView(buttonContainer)
    }

}
