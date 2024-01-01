package com.danielgergely.komp

import com.danielgergely.komp.flat.FlatAppContext
import com.danielgergely.komp.flat.FlatApplication
import com.danielgergely.komp.flat.camera.Camera
import com.danielgergely.komp.flat.canvas.FlatCanvas
import com.danielgergely.komp.flat.canvas.drawImage
import com.danielgergely.komp.flat.pipeline.PipelineDevice
import com.danielgergely.komp.flat.texture.Texture
import com.danielgergely.komp.flat.texture.TextureManager
import com.danielgergely.komp.input.KeyboardService
import com.danielgergely.komp.utils.Clock
import com.danielgergely.komp.utils.half
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

const val tilesetGridSize = 16

const val VIEWPORT_WIDTH = 9
const val VIEWPORT_HEIGHT = 9

class Sample2dApp(
    private val context: FlatAppContext,
) : FlatApplication, KeyboardService.KeyboardListener {

    private val clock = Clock()
    private var texture: Texture? = null
    private val arenaCamera: Camera = context.cameraFactory.create()

    private var state = GameState(::updateCamera)

    init {
        context.keyboardService?.addKeyboardListener(this)
    }

    override fun onSurfaceCreated(textureManager: TextureManager, pipelineDevice: PipelineDevice) {
        context.applicationScope.launch {
            texture = textureManager.loadTexture("Color_Tileset.png")
        }
        clock.start()
    }

    override fun onSurfaceSizeSet(
        textureManager: TextureManager, width: Int, height: Int, pipelineDevice: PipelineDevice
    ) = updateCamera()

    private fun updateCamera() {
        arenaCamera.setByBounds(
            worldTop = state.cameraPosition.y + 0.5f + VIEWPORT_HEIGHT.toFloat().half(),
            worldBottom = state.cameraPosition.y + 0.5f - VIEWPORT_HEIGHT.toFloat().half(),
            worldLeft = state.cameraPosition.x + 0.5f - VIEWPORT_WIDTH.toFloat().half(),
            worldRight = state.cameraPosition.x + 0.5f + VIEWPORT_WIDTH.toFloat().half(),
            screenTop = 10f / 11f,
            screenBottom = 1f / 11f,
            screenLeft = 1f / 17f,
            screenRight = 10f / 17f,
        )
    }

    override fun drawFrame(textureManager: TextureManager, canvas: FlatCanvas, pipelineDevice: PipelineDevice) {
        context.flatCanvasSettings.setClearColor(0.2f, 0.4f, 0.7f)
        texture ?: return
        updateCamera()

        val cameraX = state.cameraPosition.x
        val cameraY = state.cameraPosition.y

        for (x in cameraX - 4 until cameraX + 5) {
            for (y in cameraY - 4 until cameraY + 5) {
                canvas.drawSprite(
                    centerX = x.toFloat() + 0.5f,
                    centerY = y.toFloat() + 0.5f,
                    spriteIndex = 0,
                )
            }
        }

        state.world.objects.sortedBy { it.z }.forEach {
            if (it.x >= cameraX - VIEWPORT_WIDTH / 2 && it.x <= cameraX + VIEWPORT_WIDTH / 2 &&
                it.y >= cameraY - VIEWPORT_HEIGHT / 2 && it.y <= cameraY + VIEWPORT_HEIGHT / 2
            ) {
                canvas.drawSprite(
                    centerX = it.x + 0.5f,
                    centerY = it.y + 0.5f,
                    it.sprite.spriteIndex,
                )
            }
        }
    }

    override fun onPause() = clock.stop()
    override fun onResume() = clock.start()

    private fun FlatCanvas.drawSprite(
        centerX: Float,
        centerY: Float,
        spriteIndex: Int,
    ) {

        val spriteY = 15 - spriteIndex.mod(16)
        val spriteX = spriteIndex / 16


        this.drawImage(
            camera = arenaCamera,
            texture = texture!!,
            centerX = centerX,
            centerY = centerY,
            width = 1f,
            height = 1f,
            textureCoordinateLeft = spriteX * (1f / tilesetGridSize),
            textureCoordinateBottom = spriteY * (1f / tilesetGridSize),
            textureCoordinateRight = (spriteX + 1) * (1f / tilesetGridSize),
            textureCoordinateTop = (spriteY + 1) * (1f / tilesetGridSize),
        )
    }

    override fun onKeyDown(keyCode: Short): Boolean {
        state.world.objects.filterIsInstance<GameInputListener>().forEach { curr ->
            when (keyCode) {
                KeyboardService.KEY_DOWN -> curr.onDown()
                KeyboardService.KEY_UP -> curr.onUp()
                KeyboardService.KEY_RIGHT -> curr.onRight()
                KeyboardService.KEY_LEFT -> curr.onLeft()
                KeyboardService.KEY_SPACE -> {
                    state.loadMap()
                    updateCamera()
                }

                else -> return false
            }
        }
        return true
    }

    override fun onKeyUp(keyCode: Short): Boolean = true
}

interface GameInputListener {
    fun onUp()
    fun onLeft()
    fun onDown()
    fun onRight()
}

interface WorldObject {
    val sprite: Sprite

    val x: Int
    val y: Int
    val z: Int
}

class Wall(override val x: Int, override val y: Int) : WorldObject {
    override val sprite: Sprite = Sprite(1)
    override val z get() = 1
}

class Player(
    override var x: Int,
    override var y: Int,
    private val state: GameState,
) : WorldObject,
    GameInputListener {
    override val sprite
        get() = Sprite(
            spriteIndex = when (direction) {
                Direction.UP -> 156
                Direction.LEFT -> 157
                Direction.DOWN -> 158
                Direction.RIGHT -> 159
            }
        )

    private fun onPlayerPositionChanged() {
        val cameraX = min(max(x, VIEWPORT_WIDTH / 2), state.world.width - VIEWPORT_WIDTH / 2)
        val cameraY = min(max(y, VIEWPORT_HEIGHT / 2), state.world.height - VIEWPORT_HEIGHT / 2)
        state.cameraPosition = Position(cameraX, cameraY)
        state.onCameraUpdated()

        val objects = state.world.findObjectsByCoordinates(x, y)
        for (o in objects) {
            if (o is InteractsWithPlayer) {
                o.onCollisionWithPlayer()
            }
        }
    }

    private var direction: Direction = Direction.DOWN

    override fun onUp() {
        direction = Direction.UP
        if (canPassToCoordinate(x, y + 1)) {
            y += 1
            onPlayerPositionChanged()
        }
    }

    override fun onLeft() {
        direction = Direction.LEFT
        if (canPassToCoordinate(x - 1, y)) {
            x -= 1
            onPlayerPositionChanged()
        }
    }

    override fun onDown() {
        direction = Direction.DOWN
        if (canPassToCoordinate(x, y - 1)) {
            y -= 1
            onPlayerPositionChanged()
        }
    }

    override fun onRight() {
        direction = Direction.RIGHT
        if (canPassToCoordinate(x + 1, y)) {
            x += 1
            onPlayerPositionChanged()
        }
    }

    private fun canPassToCoordinate(x: Int, y: Int): Boolean {
        if (x < 0 || x > state.world.width) return false
        if (y < 0 || y > state.world.height) return false

        if (state.world.findObjectsByCoordinates(x, y).any { it is Wall }) return false
        if (state.chipsRemaining != 0 && state.world.findObjectsByCoordinates(x, y).any { it is StageLock }) return false

        return true
    }

    override val z: Int get() = 2
}

class Chip(
    override val x: Int,
    override val y: Int,
    private val state: GameState,
) : WorldObject, InteractsWithPlayer {
    override val sprite: Sprite = Sprite(2)
    override val z: Int = 2
    override fun onCollisionWithPlayer() {
        state.chipsRemaining -= 1
        state.chipsRemaining = max(0, state.chipsRemaining)
        state.world.objects.remove(this)
    }
}

enum class Direction {
    UP, LEFT, DOWN, RIGHT,
}

data class Sprite(val spriteIndex: Int)

class World(
    val width: Int,
    val height: Int,
) {
    val objects: MutableList<WorldObject> = mutableListOf()

    fun findObjectsByCoordinates(x: Int, y: Int): List<WorldObject> =
        objects.filter { it.x == x && it.y == y }
}

data class Position(val x: Int, val y: Int)

class GameState(val onCameraUpdated: () -> Unit) {

    val world: World get() = _world!!

    var chipsRemaining = 0

    //    val player: Player get() = _player!!
    var cameraPosition = Position(5, 5)

    private var _world: World? = null
    private var _player: Player? = null

    init {
        loadMap()
    }

    fun loadMap() {
        val player = Player(5, 5, this)
        val world = World(12, 12)
        cameraPosition = Position(5, 5)
        world.objects.add(player)
        world.objects.add(Wall(4, 5))
        world.objects.add(Wall(4, 4))
        world.objects.add(Wall(4, 1))
        world.objects.add(Wall(3, 1))
        world.objects.add(Wall(2, 1))
        world.objects.add(Wall(1, 1))
        world.objects.add(Wall(1, 2))
        world.objects.add(Wall(1, 3))
        world.objects.add(Wall(2, 3))
        world.objects.add(Wall(1, 5))
        world.objects.add(Wall(0, 5))
        world.objects.add(Wall(0, 4))
        world.objects.add(Wall(0, 3))
        world.objects.add(Wall(2, 5))
        world.objects.add(Wall(3, 5))
        world.objects.add(StageFinish(1, 4, this))
        world.objects.add(Chip(2, 2, this))
        world.objects.add(Chip(3, 2, this))
        world.objects.add(Chip(4, 2, this))
        world.objects.add(Chip(4, 3, this))
        world.objects.add(StageLock(2, 4, this))
        _world = world
        _player = player
        chipsRemaining = 4
    }
}

interface InteractsWithPlayer {
    fun onCollisionWithPlayer()
}

class StageLock(
    override val x: Int,
    override val y: Int
    , private val state: GameState,
) : WorldObject, InteractsWithPlayer {
    override val sprite: Sprite = Sprite(34)
    override val z: Int = 1

    override fun onCollisionWithPlayer() {
        state.world.objects.remove(this)
    }
}

class StageFinish(
    override val x: Int,
    override val y: Int,
    private val state: GameState,
) : WorldObject, InteractsWithPlayer {
    override val sprite: Sprite = Sprite(21)
    override val z: Int = 1

    override fun onCollisionWithPlayer() {
        state.loadMap()
    }

}