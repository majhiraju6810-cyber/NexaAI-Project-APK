package com.nexa.ai.branding

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.nexa.ai.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The official logo pieces, cut from the supplied NEXA AI logo (see tools/prepare_assets.py). */
class NexaImages(
    val nSymbol: ImageBitmap,
    val wordmark: ImageBitmap,
    val tagline: ImageBitmap,
)

/** Decodes off the main thread so the first frame is never blocked. */
suspend fun loadNexaImages(context: Context): NexaImages = withContext(Dispatchers.IO) {
    val res = context.resources
    fun load(id: Int): ImageBitmap {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        val bmp = requireNotNull(BitmapFactory.decodeResource(res, id, opts)) {
            "Could not decode resource $id"
        }
        return bmp.asImageBitmap()
    }
    NexaImages(
        nSymbol = load(R.drawable.nexa_n_symbol),
        wordmark = load(R.drawable.nexa_wordmark),
        tagline = load(R.drawable.nexa_tagline),
    )
}
