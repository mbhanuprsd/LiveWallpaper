package com.acalmindian.livewallpaper.tron

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import java.util.Random

// Data class to represent a single glowing line trail
data class TronTrail(
    var x: Float, // Current X position of the head of the trail
    var y: Float, // Current Y position of the head of the trail
    val startX: Float, // Starting X for the entire trail's path
    val startY: Float, // Starting Y for the entire trail's path
    val color: Int, // Color of this specific trail
    val speed: Float, // Speed at which this trail moves
    val direction: Int, // 0: Right, 1: Down, 2: Left, 3: Up
    var length: Float, // Current length of the trail being drawn
    val maxLength: Float, // Maximum length the trail can reach
    var alpha: Int // Current alpha for fading
) {
    // Paint object for this trail, initialized with glow effect
    val paint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 4f // Width of the line
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND // Round ends for lines
        setShadowLayer(10f, 0f, 0f, color) // Glow effect
    }

    // Path object to draw the trail
    val path: Path = Path()

    init {
        // Set initial color and alpha for the paint
        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    // Updates the position and state of the trail
    fun update(canvasWidth: Int, canvasHeight: Int, glowRadius: Float) {
        // Update glow radius in case it changed in settings
        paint.setShadowLayer(glowRadius, 0f, 0f, color)

        // Move the head of the trail
        when (direction) {
            0 -> x += speed // Right
            1 -> y += speed // Down
            2 -> x -= speed // Left
            3 -> y -= speed // Up
        }

        // Increase length until max length is reached
        if (length < maxLength) {
            length += speed
        } else {
            // Once max length is reached, start fading out
            if (alpha > 0) {
                alpha -= 5 // Adjust fade speed as needed
                if (alpha < 0) alpha = 0
                paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            }
        }

        // Rebuild the path based on current head and length
        path.reset()
        path.moveTo(startX, startY)
        when (direction) {
            0 -> path.lineTo(x, startY) // Right
            1 -> path.lineTo(startX, y) // Down
            2 -> path.lineTo(x, startY) // Left
            3 -> path.lineTo(startX, y) // Up
        }
    }

    // Draws the trail on the canvas
    fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    // Checks if the trail has faded out completely or moved off-screen
    fun isFinished(canvasWidth: Int, canvasHeight: Int): Boolean {
        return alpha <= 0 ||
                (direction == 0 && x > canvasWidth + maxLength) ||
                (direction == 1 && y > canvasHeight + maxLength) ||
                (direction == 2 && x < -maxLength) ||
                (direction == 3 && y < -maxLength)
    }
}


// Main TronRenderer class to manage and draw multiple TronTrail objects
class TronRenderer {

    // --- Configuration Variables (now dynamic) ---
    private var lineColor: Int = Color.CYAN // Default Tron blue
    private var glowRadius: Float = 10f // Default glow radius
    private var lineSpeed: Float = 10f // Default speed of lines
    private var maxLines: Int = 5 // Max number of concurrent lines

    // --- Internal State ---
    private var width: Int = 0 // Width of the canvas
    private var height: Int = 0 // Height of the canvas
    private val random = Random() // Random number generator

    private val trails = mutableListOf<TronTrail>() // List of active trails

    // Initialize the renderer with canvas dimensions and configuration parameters.
    // This should be called when the surface is created or changed, and when settings are updated.
    fun initialize(
        canvasWidth: Int,
        canvasHeight: Int,
        lineColor: Int,
        glowRadius: Float,
        lineSpeed: Float,
        maxLines: Int
    ) {
        this.width = canvasWidth
        this.height = canvasHeight
        this.lineColor = lineColor
        this.glowRadius = glowRadius
        this.lineSpeed = lineSpeed
        this.maxLines = maxLines

        // Clear existing trails and re-populate if dimensions change significantly
        // Or if the number of lines changes.
        // Only clear and re-add if the list is empty or the maxLines count has changed,
        // otherwise, existing trails will continue.
        if (trails.isEmpty() || trails.size != maxLines) {
            trails.clear()
            for (i in 0 until maxLines) {
                trails.add(createRandomTrail())
            }
        }
    }

    // Creates a new TronTrail with random starting position and direction
    private fun createRandomTrail(): TronTrail {
        val direction = random.nextInt(4) // 0:Right, 1:Down, 2:Left, 3:Up
        val startX: Float
        val startY: Float
        val maxLength: Float

        when (direction) {
            0 -> { // Right
                startX = 0f
                startY = random.nextFloat() * height
                maxLength = width.toFloat()
            }
            1 -> { // Down
                startX = random.nextFloat() * width
                startY = 0f
                maxLength = height.toFloat()
            }
            2 -> { // Left
                startX = width.toFloat()
                startY = random.nextFloat() * height
                maxLength = width.toFloat()
            }
            3 -> { // Up
                startX = random.nextFloat() * width
                startY = height.toFloat()
                maxLength = height.toFloat()
            }
            else -> { // Default to Right (should not happen with random.nextInt(4))
                startX = 0f
                startY = random.nextFloat() * height
                maxLength = width.toFloat()
            }
        }
        // Initial alpha is 255 (fully opaque)
        return TronTrail(startX, startY, startX, startY, lineColor, lineSpeed, direction, 0f, maxLength, 255)
    }

    // This method is called to draw a single frame of the Tron effect.
    // Call this repeatedly (e.g., in a loop) to create the animation.
    fun draw(canvas: Canvas) {
        // Fill the background with solid black
        canvas.drawColor(Color.BLACK)

        // Update and draw each trail
        val trailsToRemove = mutableListOf<TronTrail>()
        for (trail in trails) {
            trail.update(width, height, glowRadius)
            trail.draw(canvas)
            if (trail.isFinished(width, height)) {
                trailsToRemove.add(trail)
            }
        }

        // Remove finished trails and add new ones to maintain maxLines
        trails.removeAll(trailsToRemove)
        while (trails.size < maxLines) {
            trails.add(createRandomTrail())
        }
    }
}
