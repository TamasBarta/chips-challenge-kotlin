package com.danielgergely.komp.sample.flat

import com.danielgergely.komp.Sample2dApp
import com.danielgergely.komp.android.FlatAppActivity
import com.danielgergely.komp.flat.FlatAppContext
import com.danielgergely.komp.flat.FlatApplication
import com.danielgergely.komp.kgl.KompGlApplication
import com.danielgergely.komp.kgl.KompGlApplicationContext

class MainActivity : FlatAppActivity() {

    override fun onCreateFlatApp(flatAppContext: FlatAppContext): FlatApplication {
        return Sample2dApp(flatAppContext)
    }
}
