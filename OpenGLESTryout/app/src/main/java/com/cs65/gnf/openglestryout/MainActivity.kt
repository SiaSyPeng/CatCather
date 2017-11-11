package com.cs65.gnf.openglestryout

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.content.ContentValues.TAG
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.setLookAtM
import javax.microedition.khronos.opengles.GL10
import android.opengl.Matrix.frustumM





/**
 * Created by naman on 03-Nov-17.
 */
class MainActivity: Activity() {
    private lateinit var myGameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myGameView = GameView(this)
        setContentView(myGameView)
    }

    override fun onPause() {
        super.onPause()
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        myGameView.onPause()
    }

    override fun onResume() {
        super.onResume()
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        myGameView.onResume()
    }
}

class GameView(ctx: Context): GLSurfaceView(ctx) {

    private lateinit var gameRenderer: GameRenderer

    init{
        setEGLContextClientVersion(2)
        gameRenderer = GameRenderer(ctx)
        setRenderer(gameRenderer)
    }
}

class GameRenderer(val gameCtx: Context): GLSurfaceView.Renderer {
    companion object {
        private val TAG = "GameRenderer"
        val mMVPMatrix = FloatArray(16)
        val mProjectionMatrix = FloatArray(16)
        val mViewMatrix = FloatArray(16)
    }
    private lateinit var starfield: Starfield

    fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        starfield = Starfield()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7)

    }

    override fun onDrawFrame(unused: GL10) {
        val matrix = FloatArray(16)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        starfield.draw(mMVPMatrix)

    }

    fun loadShader(type: Int, shaderCode: String): Int {

        val shader = GLES20.glCreateShader(type)

        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    fun checkGlError(glOperation: String) {
        val error: Int
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error)
            throw RuntimeException(glOperation + ": glError " + error)
        }
    }


}

class Starfield {

}
