package com.umain.hylla.fleet

import android.content.res.AssetManager

/** Reads the bundled `devices.json`. */
fun AssetManager.loadFleet(): Fleet =
    open("devices.json").bufferedReader().use { FleetFixture.parse(it.readText()) }
