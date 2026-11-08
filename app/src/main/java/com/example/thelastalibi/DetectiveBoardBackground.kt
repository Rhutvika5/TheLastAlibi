
package com.example.thelastalibi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class DetectiveBoardBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sx = width / 1600f
        val sy = height / 900f

        canvas.save()
        canvas.scale(sx, sy)

        // Dark noir background
        paint.shader = RadialGradient(
            800f, 420f, 850f,
            Color.rgb(48, 29, 17),
            Color.rgb(4, 3, 2),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, 1600f, 900f, paint)
        paint.shader = null

        // Subtle paper texture
        val random = Random(409)
        repeat(650) {
            val alpha = random.nextInt(5, 23)
            paint.color = Color.argb(alpha, 220, 180, 130)
            canvas.drawCircle(
                random.nextFloat() * 1600f,
                random.nextFloat() * 900f,
                random.nextFloat() * 1.5f,
                paint
            )
        }

        // Aged documents on the left
        paint.color = Color.argb(34, 177, 137, 91)
        canvas.save()
        canvas.rotate(-13f, 280f, 650f)
        canvas.drawRoundRect(
            RectF(60f, 460f, 500f, 860f),
            5f, 5f, paint
        )

        paint.color = Color.argb(42, 215, 185, 140)
        repeat(8) { i ->
            canvas.drawRect(
                100f, 510f + i * 27f,
                420f, 512f + i * 27f,
                paint
            )
        }
        canvas.restore()

        // Document on upper-right
        paint.color = Color.argb(29, 207, 177, 130)
        canvas.save()
        canvas.rotate(-9f, 1380f, 190f)
        canvas.drawRect(
            1230f, 70f, 1570f, 420f, paint
        )
        paint.color = Color.argb(35, 225, 195, 145)
        repeat(7) { i ->
            canvas.drawRect(
                1260f, 120f + i * 27f,
                1515f, 122f + i * 27f,
                paint
            )
        }
        canvas.restore()

        // Red investigation strings
        paint.color = Color.argb(115, 116, 20, 18)
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE

        val stringPath = Path().apply {
            moveTo(300f, 480f)
            lineTo(650f, 630f)
            lineTo(930f, 760f)
            moveTo(650f, 630f)
            lineTo(1010f, 650f)
            lineTo(1280f, 770f)
            moveTo(300f, 480f)
            lineTo(650f, 630f)
            lineTo(620f, 850f)
        }
        canvas.drawPath(stringPath, paint)
        paint.style = Paint.Style.FILL

        // Red pins
        listOf(
            300f to 480f,
            650f to 630f,
            930f to 760f,
            1010f to 650f,
            1280f to 770f
        ).forEach { (x, y) ->
            paint.color = Color.rgb(105, 18, 18)
            canvas.drawCircle(x, y, 10f, paint)
            paint.color = Color.argb(100, 230, 90, 65)
            canvas.drawCircle(x - 2f, y - 3f, 3f, paint)
        }

        // Large fingerprint on left
        paint.color = Color.argb(27, 220, 190, 150)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f

        for (i in 0..11) {
            val radius = 38f + i * 10f
            val oval = RectF(
                55f - radius,
                315f - radius * 1.3f,
                55f + radius,
                315f + radius * 1.3f
            )
            canvas.drawArc(oval, 25f + i * 3f, 285f - i * 7f, false, paint)
        }
        paint.style = Paint.Style.FILL

        // Detective silhouette on the right
        paint.color = Color.argb(105, 2, 2, 2)

        val figure = Path().apply {
            moveTo(1320f, 900f)
            lineTo(1340f, 620f)
            lineTo(1305f, 540f)
            lineTo(1350f, 465f)
            lineTo(1360f, 370f)
            lineTo(1395f, 315f)
            lineTo(1435f, 310f)
            lineTo(1470f, 350f)
            lineTo(1480f, 435f)
            lineTo(1520f, 500f)
            lineTo(1550f, 600f)
            lineTo(1600f, 720f)
            lineTo(1600f, 900f)
            close()
        }
        canvas.drawPath(figure, paint)

        // Detective hat
        paint.color = Color.argb(135, 1, 1, 1)
        canvas.drawOval(
            RectF(1360f, 245f, 1510f, 290f), paint
        )
        val hat = Path().apply {
            moveTo(1380f, 260f)
            lineTo(1400f, 215f)
            lineTo(1450f, 185f)
            lineTo(1490f, 220f)
            lineTo(1510f, 265f)
            close()
        }
        canvas.drawPath(hat, paint)

        // Distant Greywood skyline
        paint.color = Color.argb(100, 5, 5, 5)

        for (i in 0..15) {
            val x = 950f + i * 40f
            val h = 90f + random.nextFloat() * 90f
            canvas.drawRect(
                x, 870f - h,
                x + 36f, 900f, paint
            )
        }

        // Clock tower
        canvas.drawRect(
            1200f, 650f, 1275f, 900f, paint
        )
        canvas.drawRect(
            1218f, 600f, 1258f, 650f, paint
        )

        val towerTop = Path().apply {
            moveTo(1190f, 600f)
            lineTo(1240f, 545f)
            lineTo(1290f, 600f)
            close()
        }
        canvas.drawPath(towerTop, paint)

        // Street lamp glow
        paint.shader = RadialGradient(
            1150f, 720f, 135f,
            intArrayOf(
                Color.argb(65, 240, 155, 55),
                Color.argb(20, 200, 100, 35),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(1150f, 720f, 135f, paint)
        paint.shader = null

        // Vignette
        paint.shader = RadialGradient(
            800f, 450f, 790f,
            Color.TRANSPARENT,
            Color.argb(190, 0, 0, 0),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, 1600f, 900f, paint)
        paint.shader = null

        canvas.restore()
    }
}
