import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.KglJs
import com.danielgergely.komp.Sample2dApp
import com.danielgergely.komp.glAppFromFlatApp
import com.danielgergely.komp.js.BrowserGlAppContext
import com.danielgergely.komp.js.JsKeyboardService
import com.danielgergely.komp.kgl.KompGlApplication
import kotlinx.browser.document
import kotlinx.browser.window

lateinit var context: BrowserGlAppContext
lateinit var app: KompGlApplication
lateinit var kgl: Kgl

fun main() {
    document.addEventListener("DOMContentLoaded", {
        init()
    })
}

fun init() {
    val canvas = document.getElementById("canvas")!!

    context = BrowserGlAppContext(
        canvas = canvas,
        resourcesUrlPrefix = "/resources/"
    )
    app = glAppFromFlatApp(context) { flatAppContext ->
        Sample2dApp(flatAppContext)
    }

    val gl = canvas.asDynamic().getContext("webgl2", js("{\"alpha\": false}"))
    kgl = KglJs(gl)

    app.onSurfaceCreated(kgl)
    app.onSurfaceSizeSet(kgl, 544, 352)

    app.onResume()

    window.requestAnimationFrame(::render)
    JsKeyboardService.init()
}

fun render(now: Double) {
    app.drawFrame(kgl)
    window.requestAnimationFrame(::render)
}
