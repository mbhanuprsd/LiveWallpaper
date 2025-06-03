package com.acalmindian.livewallpaper.matrix
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.util.Random

// This class encapsulates the logic for drawing the Matrix Rain effect.
// You would typically instantiate this class within your WallpaperService.Engine
// and call its draw method on each frame update.
class MatrixRainRenderer {

    // --- Configuration Variables (now dynamic, removed hardcoded values) ---
    private var textSize: Float = 0f
    private var fadeSpeed: Int = 0
    private var brightGreenColor: Int = 0
    private var darkGreenColor: Int = 0

    private val DROP_SPEED_MIN = 10 // Minimum speed for a rain drop
    private val DROP_SPEED_MAX = 30 // Maximum speed for a rain drop
    private val FONT_FAMILY = Typeface.MONOSPACE // Monospace font for consistent character width

    // --- Internal State ---
    private var width: Int = 0 // Width of the canvas
    private var height: Int = 0 // Height of the canvas
    private var columns: Int = 0 // Number of columns based on text size
    private lateinit var drops: IntArray // Array to store the Y position of each drop
    private lateinit var paints: Array<Paint> // Array of paints for different fade levels
    private val random = Random() // Random number generator

    // Characters that will be displayed in the rain.
    // This can now be changed dynamically via setCharacters.
    private var matrixChars = "アァカサタナハマヤラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユルグズヅブプエェケセテネヘメレヱゲゼデベペオォコソトノホモヨロヲゴゾドボポヴッン"

    // Initialize the renderer with the canvas dimensions and new configuration parameters.
    // This should be called when the surface is created or changed, and when settings are updated.
    fun initialize(
        canvasWidth: Int,
        canvasHeight: Int,
        fadeSpeed: Int,
        textSize: Float,
        brightGreenColor: Int,
        darkGreenColor: Int
    ) {
        this.width = canvasWidth
        this.height = canvasHeight
        this.fadeSpeed = fadeSpeed
        this.textSize = textSize
        this.brightGreenColor = brightGreenColor
        this.darkGreenColor = darkGreenColor

        // Calculate number of columns based on canvas width and text size.
        // Ensure at least one column.
        this.columns = if (this.textSize > 0) (width / this.textSize).toInt() else 1
        if (this.columns == 0) this.columns = 1 // Ensure at least one column even if text size is large

        this.drops = IntArray(columns) // Initialize drops array

        // Set initial random Y positions for each drop, starting off-screen at the top
        for (i in 0 until columns) {
            drops[i] = -random.nextInt(height) // Start randomly above the screen
        }

        // Pre-create paint objects for performance.
        // We'll have a bright paint for the head and fading paints for the tail.
        paints = Array(this.fadeSpeed + 1) { index ->
            Paint().apply {
                color = if (index == 0) this@MatrixRainRenderer.brightGreenColor else this@MatrixRainRenderer.darkGreenColor
                // Gradually decrease alpha for fading effect
                alpha = 255 - (index * (255 / this@MatrixRainRenderer.fadeSpeed))
                this.textSize = this@MatrixRainRenderer.textSize
                typeface = FONT_FAMILY
                isAntiAlias = true // Smooth out text edges
            }
        }
    }

    // New method to update the characters used in the rain.
    fun setCharacters(newChars: String) {
        if (newChars.isNotBlank()) { // Ensure the new string is not empty
            this.matrixChars = newChars
        } else {
            // Revert to default characters if an empty string is provided
            this.matrixChars = "アァカサタナハマヤラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユルグズヅブプエェケセテネヘメレヱゲゼデベペオォコソトノホモヨロヲゴゾドボポヴッン"
        }
    }

    // This method is called to draw a single frame of the Matrix Rain.
    // Call this repeatedly (e.g., in a loop) to create the animation.
    fun draw(canvas: Canvas) {
        // Fill the background with a semi-transparent black to create the fade effect
        // The alpha value (40) controls how quickly old characters fade away.
        canvas.drawColor(Color.argb(40, 0, 0, 0)) // Alpha adjusted for a denser look

        // Iterate through each column (drop)
        for (i in 0 until columns) {
            // Draw characters for the current drop, fading as they go down
            for (j in 0..fadeSpeed) {
                // Ensure we have characters to draw from
                if (matrixChars.isEmpty()) {
                    // Fallback to default if somehow characters are empty, or skip drawing for this frame
                    matrixChars = "アァカサタナハマヤラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユルグズヅブプエェケセテネヘメレヱゲゼデベペオォコソトノホモヨロヲゴゾドボポヴッン"
                    continue
                }

                // Get a random character from the defined set
                val charIndex = random.nextInt(matrixChars.length)
                val character = matrixChars[charIndex].toString()

                // Calculate the Y position for the current character in the drop.
                // drops[i] is the Y-coordinate of the leading (bottom-most) character.
                // Subsequent characters in the drop are drawn above it.
                val y = drops[i] - (j * textSize)

                // Only draw if the character is within the canvas bounds
                // We add textSize to height to ensure characters are drawn even if their top is off-screen
                if (y > -textSize && y < height + textSize) {
                    canvas.drawText(character, i * textSize, y, paints[j])
                }
            }

            // Move the drop down. Speed varies randomly within the defined range.
            drops[i] += random.nextInt(DROP_SPEED_MAX - DROP_SPEED_MIN + 1) + DROP_SPEED_MIN

            // If the leading character of the drop goes off the bottom of the screen,
            // reset the entire drop to a random position above the screen.
            if (drops[i] > height) {
                drops[i] = -random.nextInt(height) // Reset to a random negative Y to fall in from above
            }
        }
    }
}
