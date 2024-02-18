package ishino.kzk.view_to_video.encoder

import android.content.Context
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/** OpenGL と MediaCodec を使わずに [CanvasProcessor] をする */
object CanvasProcessorHighLevelApi {

    /**
     * 処理を開始する
     *
     * @param context [Context]
     * @param resultFile エンコード先のファイル
     * @param videoCodec 動画コーデック
     * @param containerFormat コンテナフォーマット
     * @param bitRate ビットレート
     * @param frameRate フレームレート
     * @param outputVideoWidth 動画の高さ
     * @param outputVideoHeight 動画の幅
     * @param onCanvasDrawRequest Canvasの描画が必要になったら呼び出される。trueを返している間、動画を作成する
     */
    suspend fun start(
        context: Context,
        resultFile: File,
        videoCodec: Int = MediaRecorder.VideoEncoder.H264,
        containerFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
        bitRate: Int = 1_000_000,
        frameRate: Int = 60,
        outputVideoWidth: Int = 720,
        outputVideoHeight: Int = 1280,
        onCanvasDrawRequest: Canvas.(positionMs: Long) -> Boolean,
    ) = withContext(Dispatchers.Default) {
        val mediaRecorder =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()).apply {
                // メソッド呼び出しには順番があります
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(containerFormat)
                setVideoEncoder(videoCodec)
                setVideoEncodingBitRate(bitRate)
                setVideoFrameRate(frameRate)
                setVideoSize(outputVideoWidth, outputVideoHeight)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(resultFile.path)
                prepare()
            }
        val inputSurface = mediaRecorder.surface
        val startTime = System.currentTimeMillis()
        mediaRecorder.start()
        var isRunning = true
        while (isActive) {
            val positionMs = System.currentTimeMillis() - startTime
            val canvas = inputSurface.lockHardwareCanvas()
            isRunning = onCanvasDrawRequest(canvas, positionMs)
            try {
                inputSurface.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                Timber.d("debug/error: ${e.message}")
            }
            if (!isRunning) {
                Timber.d("debug/break isRunning: $isRunning")
                break
            }
        }
        Timber.d("debug/isRunning: $isRunning")
        mediaRecorder.stop()
        Timber.d("debug/mediaRecorder.stop()")
        mediaRecorder.release()
        Timber.d("debug/mediaRecorder.release()")
    }
}