package it.vixstreamcs

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class VixStreamPlugin: Plugin() {
    override fun load(context: Context) {
        // All providers should be added here.
        registerMainAPI(VixStreamCS())
        registerExtractorAPI(VixSrcExtractor())
    }
}
