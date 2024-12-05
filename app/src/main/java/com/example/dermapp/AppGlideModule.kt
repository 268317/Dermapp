package com.example.dermapp

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Custom Glide module for configuring Glide settings.
 * This disables automatic manifest parsing for Glide.
 */
@GlideModule
class AppGlideModule : AppGlideModule() {

    /**
     * Disables automatic manifest parsing to improve initialization performance.
     *
     * @return false to disable manifest parsing.
     */
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
