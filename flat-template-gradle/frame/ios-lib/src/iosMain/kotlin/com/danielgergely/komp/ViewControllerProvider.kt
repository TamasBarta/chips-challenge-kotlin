package com.danielgergely.komp

import com.danielgergely.komp.ios.ResourceNameProvider
import com.danielgergely.komp.metal.FlatMetalViewControllerFactory
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

class ViewControllerProvider {

    fun getViewController(): UIViewController {
        return FlatMetalViewControllerFactory().createFlatMetalViewController(
            bundle = getBundle(),
            resourceNameProvider = getResourceNaming(),
            appFactory = { Sample2dApp(it) },
        )
    }

    private fun getBundle(): NSBundle {
        return NSBundle.bundleWithIdentifier(BUNDLE_ID)!!
    }

    private fun getResourceNaming() = ResourceNameProvider { path ->
        path
            .replace("_", "_u")
            .replace("/", "_s")
    }

    private companion object {
        const val BUNDLE_ID = "com.danielgergely.kgl.KompIos"
    }
}
