package com.callcenter.smartclass.ui.home.article

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Base64
import android.util.Log
import android.widget.TextView

class Base64ImageGetter(private val textView: TextView) : Html.ImageGetter {

    override fun getDrawable(source: String): Drawable? {
        if (source.startsWith("data:image/")) {
            try {
                Log.d("Base64ImageGetter", "Found base64 image")

                val parts = source.split(";base64,")
                if (parts.size != 2) {
                    Log.e("Base64ImageGetter", "Source does not contain ';base64,'")
                    return null
                }

                val mimeType = parts[0].substringAfter("data:")
                val base64Data = parts[1]

                Log.d("Base64ImageGetter", "MIME Type: $mimeType")
                Log.d("Base64ImageGetter", "Base64 Data Length: ${base64Data.length}")

                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                Log.d("Base64ImageGetter", "Decoded Bytes Length: ${decodedBytes.size}")

                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap == null) {
                    Log.e("Base64ImageGetter", "Bitmap is null after decoding")
                    return null
                }

                val drawable = BitmapDrawable(textView.resources, bitmap)

                val textViewWidth = textView.width
                if (textViewWidth > 0) {
                    val scale = textViewWidth.toFloat() / bitmap.width
                    val scaledWidth = textViewWidth
                    val scaledHeight = (bitmap.height * scale).toInt()
                    drawable.setBounds(0, 0, scaledWidth, scaledHeight)
                    Log.d("Base64ImageGetter", "Drawable bounds set to: $scaledWidth x $scaledHeight")
                } else {
                    drawable.setBounds(0, 0, bitmap.width, bitmap.height)
                    Log.d("Base64ImageGetter", "Drawable bounds set to: ${bitmap.width} x ${bitmap.height}")
                }

                return drawable

            } catch (e: Exception) {
                Log.e("Base64ImageGetter", "Error decoding base64 image", e)
                return null
            }
        }

        return null
    }
}
