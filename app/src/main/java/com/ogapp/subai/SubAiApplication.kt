package com.ogapp.subai

import android.app.Application

/**
 * Application entry point for SubAI.
 * Kept lightweight on purpose — heavy singletons are created lazily where used
 * so cold-start stays fast.
 */
class SubAiApplication : Application()
