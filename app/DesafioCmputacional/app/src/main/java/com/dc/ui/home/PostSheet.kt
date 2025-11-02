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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.res.integerResource
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import coil.ImageLoader
import coil.load
import com.dc.R
import coil.util.DebugLogger
import com.dc.api.ApiWrapper
import com.dc.components.Drawer
import com.dc.entities.Post
import com.dc.coordinates.LatLon
import com.dc.entities.PostInteraction
import com.dc.entities.PostInteractionType
import okhttp3.OkHttpClient
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.dc.api.isSuccess
import com.dc.utils.SessionManager

class PostSheet(private var post: Post, private var interaction: PostInteraction) : Drawer() {
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
        val containerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        containerParams.topMargin = 32
        buttonContainer.layoutParams = containerParams

        val iconSizeInDp = 16
        val iconSizeInPixels = (iconSizeInDp * resources.displayMetrics.density).toInt()

        val usefulColor = Color.parseColor("#44BB55")
        val usefulButton = Button(requireContext())

        usefulButton.text = "Achei útil (${post.upvote_count})"
        usefulButton.textSize = 10f
        usefulButton.setTextColor(usefulColor)

        if ( interaction.getInteractionType() == PostInteractionType.UPVOTE) {
            usefulButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_interaction_u_s)
            usefulButton.setTextColor(Color.WHITE)
        }
        else {
            usefulButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_interaction_u)
        }

        usefulButton.setPadding(32, 0, 32, 0)

        val usefulDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_arrow_up)
        usefulDrawable?.setBounds(0, 0, iconSizeInPixels, iconSizeInPixels)
        usefulDrawable?.setTint(usefulColor)
        usefulButton.setCompoundDrawables(usefulDrawable, null, null, null)

        val usefulParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        usefulParams.marginEnd = 8
        usefulButton.layoutParams = usefulParams

        val irrelevantColor = Color.parseColor("#FF4455")
        val irrelevantButton = Button(requireContext())
        irrelevantButton.text = "Achei irrelevante (${post.downvote_count})"
        irrelevantButton.textSize = 10f
        irrelevantButton.setTextColor(irrelevantColor)

        if (interaction.getInteractionType() == PostInteractionType.DOWNVOTE) {
            irrelevantButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_interaction_d_s)
            irrelevantButton.setTextColor(Color.WHITE)
        }
        else {
            irrelevantButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_interaction_d)
        }

        irrelevantButton.setPadding(32, 0, 32, 0)

        val irrelevantDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_arrow_down)
        irrelevantDrawable?.setBounds(0, 0, iconSizeInPixels, iconSizeInPixels)
        irrelevantDrawable?.setTint(irrelevantColor)
        irrelevantButton.setCompoundDrawables(irrelevantDrawable, null, null, null)

        val irrelevantParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        irrelevantParams.marginStart = 8
        irrelevantButton.layoutParams = irrelevantParams

        // add functionality to the buttons
        setupButtonListeners(usefulButton, "UP", irrelevantButton)
        setupButtonListeners(irrelevantButton, "DOWN", usefulButton)


        buttonContainer.addView(usefulButton)
        buttonContainer.addView(irrelevantButton)

        mainLayout.addView(buttonContainer)
    }
    private fun setupButtonListeners(
        button: Button,
        interactionType: String,
        otherButton: Button
    ) {
        button.setOnClickListener {
            // Prevent multiple clicks while the request is in progress
            button.isEnabled = false
            otherButton.isEnabled = false

            Log.d("Button Cliked: ", "interacion=" + interactionType)


            // Create the interaction object to send to the API
            val id_user = SessionManager.getInstance(requireContext()).getUserId()
            if (id_user == -1) {
                Log.e("PostSheet", "User ID not found")
                return@setOnClickListener
            }
            Log.d("PostSheet", "User ID is ${id_user}")
            Log.d("PostSheet", "Post ID is ${post.id_post}")
            val newInteraction = PostInteraction(
                id_post = post.id_post,
                id_user = id_user,
                interaction = interactionType,
            )

            lifecycleScope.launch {
                try {
                    if ( newInteraction.interaction == null ) {
                        Log.e("PostSheet", "Interaction type is null")
                        return@launch
                    }

                    val response = ApiWrapper.postInteraction(
                        newInteraction.id_post,
                        newInteraction.id_user,
                        newInteraction.interaction
                    )

                    if (response.isSuccess) {
                        Log.d("PostSheet", "Interaction registered: $interactionType")
                        dismiss()
                    } else {
                        Log.e("PostSheet", "Failed to register interaction:")
                        Toast.makeText(
                            requireContext(),
                            "Falha ao registrar interação.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("PostSheet", "Exception during interaction registration", e)
                } finally {
                    button.isEnabled = true
                    otherButton.isEnabled = true
                }
            }
        }
    }

}
