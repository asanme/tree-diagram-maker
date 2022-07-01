package com.asanme.treediagrammaker

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

private const val stroke_width = 6f
class DrawFigures(context: Context): View(context) {
    private var motionX = 50f
    private var motionY = 50f

    private lateinit var holder: Canvas
    private lateinit var bmap: Bitmap
    private lateinit var path : Path

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.white, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.black, null)

    private val paint = Paint().apply{
        color = drawColor
        style = Paint.Style.STROKE
        isAntiAlias = true

        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    /*override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(::bmap.isInitialized) bmap.recycle()
        bmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        holder = Canvas(bmap)
        holder.drawColor(backgroundColor)
    }*/

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        bmap = BitmapFactory.decodeResource(resources, R.drawable.user)
        canvas?.drawBitmap(bmap, motionX, motionY, paint)
        /*path = Path
        path.moveTo(0f, 0f)
        path.lineTo(450f, 450f)
        path.close()
        canvas?.drawPath(path, paint)

        val xpos = (canvas?.width)?.div(2)
        val ypos = (canvas?.height)?.div(2 )

        if(xpos != null && ypos != null)
        {
             canvas?.drawText("HELLO", xpos.toFloat(), ypos.toFloat(), paint)
        }*/
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        motionX = event!!.x
        motionY = event!!.y
        when(event.action){
            MotionEvent.ACTION_UP -> touchUp()
            MotionEvent.ACTION_MOVE -> touchMove(motionX, motionY)
            MotionEvent.ACTION_DOWN -> touchStart(motionX, motionY)
        }

        return true
    }

    private fun touchStart(xval: Float, yval: Float) {
        motionX = xval
        motionY = yval
    }

    private fun touchMove(xval: Float, yval: Float) {
        motionX = xval
        motionY = yval
        invalidate()
    }

    private fun touchUp() {
    }
}