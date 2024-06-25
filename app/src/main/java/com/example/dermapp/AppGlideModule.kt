package com.example.dermapp

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Determines whether manifest parsing is enabled.
 *
 * @return false to disable automatic manifest parsing.
 */
@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false // Disables automatic manifest parsing
    }
}