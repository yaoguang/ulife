package com.androidx.ulife.app

import android.app.Application

lateinit var app: Application
    private set

fun initApplicationAbout(application: Application) {
    app = application
    app.apply {

    }
}