package com.dc.components

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
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

open class Drawer : DialogFragment() {

    // This is the container for the drawer's content
    protected lateinit var mainLayout: LinearLayout
    // This is the root layout that will allow scrolling
    private lateinit var scrollableLayout: android.widget.ScrollView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return initModal()
    }

    protected fun initModal(): Dialog {
        val ctx = requireContext()
        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(30f,30f,30f,30f,0f,0f,0f,0f)
            setColor(Color.WHITE)
        }

        // The inner LinearLayout which will hold all the views.
        mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                32,
                32,
                32,
                32
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // The new root ScrollView
        scrollableLayout = android.widget.ScrollView(requireContext()).apply {
            background = drawable
            addView(mainLayout)
        }

        dialog.setContentView(scrollableLayout)

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

    fun createDebugBody() {
        //  Corpo
        val btn = Button(context).apply {
            text = "Close"
            setOnClickListener { dismiss() }
        }
        // TITLE
        val title = TextView(context).apply {
            text = "This is a smooth drawer modal"
            textSize = 20f
            setTextColor(Color.BLACK)
        }

        mainLayout.addView(title)
        mainLayout.addView(createMockCarousel(requireContext()))
        mainLayout.addView(btn)
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
            translationY = (screenHeight * 0.9f)
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
