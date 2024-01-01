package com.danielgergely.komp

import com.danielgergely.kgl.KglLwjgl
import com.danielgergely.komp.kgl.KglRenderer
import com.danielgergely.komp.lwjgl.LwjglAppContext
import com.danielgergely.komp.lwjgl.getFramebufferSize
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.system.exitProcess

const val widthTiles = 17
const val heightTiles = 11
const val tileWidth = 32
const val tileHeight = 32

class LwjglProgram {
    private var window: Long = 0

    private lateinit var context: LwjglAppContext
    private lateinit var app: KglRenderer

    private val kgl = KglLwjgl

    private var width = tileWidth * widthTiles
    private var height = tileHeight * heightTiles

    fun run() {
        init()
        loop()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()

        exitProcess(0)
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.out).set()

        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)

        val primaryMonitor = glfwGetPrimaryMonitor()

        window = glfwCreateWindow(width, height, "name", NULL, NULL)
        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")

        context = LwjglAppContext(window)

        val stack = stackPush()
        try {
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            glfwGetWindowSize(window, pWidth, pHeight)

            val vidMode = glfwGetVideoMode(primaryMonitor)!!

            val width = pWidth.get(0)
            val height = pHeight.get(0)

            glfwSetWindowPos(
                window,
                (vidMode.width() - width) / 2,
                (vidMode.height() - height) / 2
            )

            context.mouseService?.apply { onResize(width, height) }
        } finally {
            stack.close()
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)

        app = glAppFromFlatApp(context) { flatAppContext ->
            Sample2dApp(flatAppContext)
        }

        glfwShowWindow(window)

        window.getFramebufferSize().let {
            this.width = it.width
            this.height = it.height
        }
    }

    private fun loop() {
        createCapabilities()

        app.onSurfaceCreated(kgl)
        app.onSurfaceSizeSet(kgl, width, height)

        while (!glfwWindowShouldClose(window)) {
            app.drawFrame(kgl)

            glfwSwapBuffers(window)

            context.runFromQueue()
            glfwPollEvents()
        }
    }
}

fun main(args: Array<String>) {
    LwjglProgram().run()
}
