package com.example.kartar.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.kartar.ARcore.helpers.CameraPermissionHelper
import com.example.kartar.ARcore.helpers.DisplayRotationHelper
import com.example.kartar.ARcore.helpers.FullScreenHelper
import com.example.kartar.ARcore.helpers.SnackbarHelper
import com.example.kartar.ARcore.helpers.TrackingStateHelper
import com.example.kartar.ARcore.rendering.AugmentedImageRenderer
import com.example.kartar.ARcore.rendering.BackgroundRenderer
import com.example.kartar.R
import com.example.kartar.controller.AugmentedController
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.firebase.appcheck.interop.BuildConfig
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AugmentedActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    /*使うかるたのデータをまとめたリスト*/
    private var pairList: MutableList<Pair<String, String>> = ArrayList()
    /*AugmentedControllerのViewModel*/
    val augmentedController = AugmentedController()

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private var surfaceView: GLSurfaceView? = null
    private var fitToScanView: ImageView? = null
    private var glideRequestManager: RequestManager? = null
    private var installRequested = false
    private var session: Session? = null
    private val messageSnackbarHelper = SnackbarHelper()
    private var displayRotationHelper: DisplayRotationHelper? = null
    private val trackingStateHelper = TrackingStateHelper(this)
    private val backgroundRenderer = BackgroundRenderer()
    private val augmentedImageRenderer = AugmentedImageRenderer()
    private var shouldConfigureSession = false

    // Augmented image configuration and rendering.
    // Load a single image (true) or a pre-generated image database (false).
    private val useSingleImage = true

    // Augmented image and its associated center pose anchor, keyed by index of the augmented image in
    // the
    // database.
    private val augmentedImageMap: MutableMap<Int, Pair<AugmentedImage, Anchor>> = HashMap()

    // 戻るボタンを無効にするためのメソッド
    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "Toast.makeText(this, \"ゲーム中は前画面に戻れません!\", Toast.LENGTH_SHORT).show()",
            "android.widget.Toast",
            "android.widget.Toast"
        )
    )
    override fun onBackPressed() {
        Toast.makeText(this, "ゲーム中は前画面に戻れません!", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmentedimage)
        surfaceView = findViewById(R.id.surfaceView)
        displayRotationHelper = DisplayRotationHelper( /*context=*/this)

        // Set up renderer.
        surfaceView?.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(2)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.
            setRenderer(this@AugmentedActivity)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            setWillNotDraw(false)
        }

        fitToScanView = findViewById(R.id.image_view_fit_to_scan)
        if (fitToScanView != null) {
            glideRequestManager = Glide.with(this)
            glideRequestManager!!
                .load(Uri.parse("file:///android_asset/fit_to_scan.png"))
                .into(fitToScanView!!)
        }
        installRequested = false

        /*画面遷移元からもらう値*/
        val keys = intent.getStringArrayExtra("KEYS")
        val values = intent.getStringArrayExtra("VALUES")
        augmentedController.roomUid.value = intent.getStringExtra("ROOMUID").toString()
        augmentedController.ownerUid.value = intent.getStringExtra("OWNERUID").toString()
        /*今回使うかるたデータをリスト化*/
        if (keys != null && values != null) {
            for (i in 0 until keys.size.coerceAtMost(values.size)) {
                pairList.add(Pair(keys[i], values[i]))
            }
        }
        /*自身の状態をrestにする*/
        Log.d("stream", augmentedController.roomUid.value)
        FirebaseSingleton.databaseReference.getReference("room/${augmentedController.roomUid.value}/player/${FirebaseSingleton.currentUid()}")
            .setValue("rest")
    }

    override fun onStart() {
        if (augmentedController.ownerUid.value == FirebaseSingleton.currentUid()) {
            augmentedController.upDatePlayerState()
        }
        augmentedController.upDateRoomState(this)

        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }

                    ArCoreApk.InstallStatus.INSTALLED -> {}
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }
                session = Session( /* context = */this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: Exception) {
                message = "This device does not support AR"
                exception = e
            }
            if (message != null) {
                messageSnackbarHelper.showError(this, message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
            shouldConfigureSession = true
        }
        if (shouldConfigureSession) {
            configureSession()
            shouldConfigureSession = false
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.")
            session = null
            return
        }
        surfaceView!!.onResume()
        displayRotationHelper!!.onResume()
        fitToScanView!!.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session!!.close()
            session = null
        }
        super.onDestroy()
    }

    public override fun onPause() {
        super.onPause()
        /*順番が重要!GLSurfaceViewは、セッションを問い合わせようとしないように最初に一時停止する。
    　SessionがGLSurfaceViewの前に一時停止されると、GLSurfaceViewはsession.update()を呼び出すことがありSessionPausedExceptionが発生するかも。*/if (session != null) {
            displayRotationHelper!!.onPause()
            surfaceView!!.onPause()
            session!!.pause()
        }
    }

    /**権限を要求＆それを許可or拒否した後に呼び出される**/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        /*カメラのpermission許可されていない場合*/if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "このアプリはカメラpermissionが必要です!", Toast.LENGTH_LONG).show()
            /*カメラのpermission拒否を2度としないに設定した場合*/if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(
                    this
                )
            ) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    /**適切なスクリーンモードに変換する */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    /**GLSurfaceViewが始めて作成された時に呼び出される */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        try {
            // 背景のテクスチャを作成してARCore セッションに渡し、update() 中に塗りつぶします。
            backgroundRenderer.createOnGlThread( /*context=*/this)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayRotationHelper!!.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (session == null) {
            return
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper!!.updateSessionIfNeeded(session)
        try {
            session!!.setCameraTextureName(backgroundRenderer.textureId)

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            val frame = session!!.update()
            val camera = frame.camera

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame)

            // Get projection matrix.
            val projmtx = FloatArray(16)
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)

            // Get camera matrix and draw.
            val viewmtx = FloatArray(16)
            camera.getViewMatrix(viewmtx, 0)

            // Compute lighting from average intensity of the image.
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

            // Visualize augmented images.
            drawAugmentedImages(frame, projmtx, viewmtx, colorCorrectionRgba)
        } catch (t: Throwable) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }
    }

    private fun configureSession() {
        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        if (!setupAugmentedImageDatabase(config)) {
            messageSnackbarHelper.showError(this, "Could not setup augmented image database")
        }
        session!!.configure(config)
    }

    /**ARの描画処理 */
    @Throws(IOException::class)
    private fun drawAugmentedImages(
        frame: Frame,
        projmtx: FloatArray,
        viewmtx: FloatArray,
        colorCorrectionRgba: FloatArray
    ) {
        val updatedAugmentedImages = frame.getUpdatedTrackables(
            AugmentedImage::class.java
        )
        /*augmentedImageMapに新規作成&削除*/
        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> if (BuildConfig.DEBUG) {  // Only log in debug builds
                    val text = String.format(
                        "Detected Image %d, %s",
                        augmentedImage.index,
                        augmentedImage.name
                    )
                    Log.d("ファイル", augmentedImage.name)
                    messageSnackbarHelper.showMessage(this, text)
                }

                TrackingState.TRACKING -> {
                    /*UIの更新*/runOnUiThread {
                        fitToScanView!!.visibility = View.GONE
                    }
                    /*始めてアンカーを取得した時の新規作成処理*/if (!augmentedImageMap.containsKey(
                            augmentedImage.index
                        )
                    ) {
                        val centerPoseAnchor =
                            augmentedImage.createAnchor(augmentedImage.centerPose)
                        augmentedImageMap[augmentedImage.index] =
                            Pair.create(augmentedImage, centerPoseAnchor)
                        if (!augmentedImageRenderer.isCreated(augmentedImage.name)) {
                            augmentedImageRenderer.createOnGlThread(this, augmentedImage.name)
                        }
                    }
                }

                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage.index)
                else -> {}
            }
        }

        /*augmentedImageMapのimageを全部AR表示する*/for (pair in augmentedImageMap.values) {
            /*first:AR表示する画像、second:アンカー画像*/
            val augmentedImage = pair.first
            val centerAnchor = pair.second
            if (augmentedImage.trackingState == TrackingState.TRACKING) {
                augmentedImageRenderer.draw(
                    viewmtx,
                    projmtx,
                    augmentedImage,
                    centerAnchor,
                    colorCorrectionRgba
                )
            }
        }
    }

    /**ゲームにつかうかるたのARDatabaseを作成 */
    private fun setupAugmentedImageDatabase(config: Config): Boolean {
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        if (useSingleImage) {
            for (pair in pairList) {
                val key = pair.first
                val value = pair.second
                val assetPath = "efuda/$key.png"
                Log.d("ファイル", "key:$key\nvalue:$value\n存在します")
                augmentedImageDatabase.addImage(value, loadAugmentedImageBitmap(assetPath))
            }
        }
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    /**assetから指定したアンカー画像を取り出す */
    private fun loadAugmentedImageBitmap(imageName: String): Bitmap? {
        try {
            assets.open(imageName).use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e)
        }
        return null
    }

    companion object {
        //ARcoreにもともとあった値
        private val TAG = AugmentedActivity::class.java.simpleName
    }
}