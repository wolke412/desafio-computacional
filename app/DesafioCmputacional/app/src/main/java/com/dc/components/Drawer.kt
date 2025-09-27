package com.dc.components

import com.dc.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class Drawer : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val ctx = requireContext()
        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(30f,30f,30f,30f,0f,0f,0f,0f)
            setColor(Color.WHITE)
        }
//        drawable.isAntiAlias = true

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            background=drawable
            setPadding(
                32,
                32,
                32,
                32
            )
            // TITLE
            addView(TextView(context).apply {
                text = "This is a smooth drawer modal"
                textSize = 20f
                setTextColor(Color.BLACK)
            })

        }


        //  Corpo
        val btn = Button(context).apply {
            text = "Close"
            setOnClickListener { dismiss() }
        }

        layout.addView(createMockCarousel(requireContext()))
        layout.addView(btn)
        dialog.setContentView(layout)

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        return dialog
    }



    fun createMockImageBox(context: Context): View {
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f    // round corners (px)
            setColor(Color.LTGRAY) // grey background
        }

        return View(context).apply {
            background = shape
            layoutParams = LinearLayout.LayoutParams(600, 840).apply {
                setMargins(8, 8, 8, 8)
            }
        }
    }


    fun createMockCarousel(context: Context): HorizontalScrollView {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add several mock boxes
        repeat(6) {
            container.addView(createMockImageBox(context))
        }

        return HorizontalScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
            addView(container)
        }
    }




    override fun onStart() {
        super.onStart()

        val screenHeight = resources.displayMetrics.heightPixels
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (screenHeight * 0.8).toInt()
        )

        // Start the smooth slide-in animation
        dialog?.window?.decorView?.apply {
            translationY = (screenHeight * 0.8f)
            animate().translationY(0f).setDuration(300).start()
        }
    }

    override fun dismiss() {
        // Smooth slide-out animation
        dialog?.window?.decorView?.apply {

            // adds padding to completely vanish it
            animate().translationY(height.toFloat() + 100).setDuration(300)
                .withEndAction { super.dismiss() }
                .start()
        } ?: super.dismiss()
    }

}
