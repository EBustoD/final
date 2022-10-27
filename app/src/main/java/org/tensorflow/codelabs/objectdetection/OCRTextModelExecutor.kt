/*package org.tensorflow.codelabs.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Random
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils.bitmapToMat
import org.opencv.android.Utils.matToBitmap
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfRotatedRect
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Size
import org.opencv.dnn.Dnn.NMSBoxesRotated
import org.opencv.imgproc.Imgproc.boxPoints
import org.opencv.imgproc.Imgproc.getPerspectiveTransform
import org.opencv.imgproc.Imgproc.warpPerspective
import org.opencv.utils.Converters.vector_RotatedRect_to_Mat
import org.opencv.utils.Converters.vector_float_to_Mat
import org.tensorflow.lite.Interpreter

class OCRTextModelExecutor(context: Context, private var useGPU: Boolean = false) : AutoCloseable{
    private val recognitionResult: ByteBuffer
    private val detectionInterpreter: Interprete
    private val recognitionInterpreter: Interpreter

    private var ratioHeight = 0.toFloat()
    private var ratioWidth = 0.toFloat()
    private var indicesMat: MatOfInt
    private var boundingBoxesMat: MatOfRotatedRect
    private var ocrResults: HashMap<String, Int>

    init {
        try {
            if (!OpenCVLoader.initDebug()) throw Exception("Unable to load OpenCV")
            else Log.d(TAG, "OpenCV loaded")
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Log.d(TAG, exceptionLog)
        }

        detectionInterpreter = getInterpreter(context, textDetectionModel, useGPU)
        // Recognition model requires Flex so we disable GPU delegate no matter user choice
        recognitionInterpreter = getInterpreter(context, textRecognitionModel, false)

        recognitionResult = ByteBuffer.allocateDirect(recognitionModelOutputSize * 8)
        recognitionResult.order(ByteOrder.nativeOrder())
        indicesMat = MatOfInt()
        boundingBoxesMat = MatOfRotatedRect()
        ocrResults = HashMap<String, Int>()
    }

    private fun getInterpreter(
        context: Context,
        modelName: String,
        useGpu: Boolean = false
    ): Interpreter {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(4)

        return Interpreter(loadModelFile(context, modelName), tfliteOptions)
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    private fun recognizeTexts(
        data: Bitmap,
        boundingBoxesMat: MatOfRotatedRect,
        indicesMat: MatOfInt
    ): Bitmap {
        val bitmapWithBoundingBoxes = data.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapWithBoundingBoxes)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.toFloat()
        paint.setColor(Color.GREEN)

        for (i in indicesMat.toArray()) {
            val boundingBox = boundingBoxesMat.toArray()[i]
            val targetVertices = ArrayList<Point>()
            targetVertices.add(Point(0.toDouble(), (recognitionImageHeight - 1).toDouble()))
            targetVertices.add(Point(0.toDouble(), 0.toDouble()))
            targetVertices.add(Point((recognitionImageWidth - 1).toDouble(), 0.toDouble()))
            targetVertices.add(
                Point((recognitionImageWidth - 1).toDouble(), (recognitionImageHeight - 1).toDouble())
            )

            val srcVertices = ArrayList<Point>()

            val boundingBoxPointsMat = Mat()
            boxPoints(boundingBox, boundingBoxPointsMat)
            for (j in 0 until 4) {
                srcVertices.add(
                    Point(
                        boundingBoxPointsMat.get(j, 0)[0] * ratioWidth,
                        boundingBoxPointsMat.get(j, 1)[0] * ratioHeight
                    )
                )
                if (j != 0) {
                    canvas.drawLine(
                        (boundingBoxPointsMat.get(j, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j, 1)[0] * ratioHeight).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 1)[0] * ratioHeight).toFloat(),
                        paint
                    )
                }
            }
            canvas.drawLine(
                (boundingBoxPointsMat.get(0, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(0, 1)[0] * ratioHeight).toFloat(),
                (boundingBoxPointsMat.get(3, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(3, 1)[0] * ratioHeight).toFloat(),
                paint
            )

            val srcVerticesMat =
                MatOfPoint2f(srcVertices[0], srcVertices[1], srcVertices[2], srcVertices[3])
            val targetVerticesMat =
                MatOfPoint2f(targetVertices[0], targetVertices[1], targetVertices[2], targetVertices[3])
            val rotationMatrix = getPerspectiveTransform(srcVerticesMat, targetVerticesMat)
            val recognitionBitmapMat = Mat()
            val srcBitmapMat = Mat()
            bitmapToMat(data, srcBitmapMat)
            warpPerspective(
                srcBitmapMat,
                recognitionBitmapMat,
                rotationMatrix,
                Size(recognitionImageWidth.toDouble(), recognitionImageHeight.toDouble())
            )

            val recognitionBitmap =
                ImageUtils.createEmptyBitmap(
                    recognitionImageWidth,
                    recognitionImageHeight,
                    0,
                    Bitmap.Config.ARGB_8888
                )
            matToBitmap(recognitionBitmapMat, recognitionBitmap)

            val recognitionTensorImage =
                ImageUtils.bitmapToTensorImageForRecognition(
                    recognitionBitmap,
                    recognitionImageWidth,
                    recognitionImageHeight,
                    recognitionImageMean,
                    recognitionImageStd
                )

            recognitionResult.rewind()
            recognitionInterpreter.run(recognitionTensorImage.buffer, recognitionResult)

            var recognizedText = ""
            for (k in 0 until recognitionModelOutputSize) {
                var alphabetIndex = recognitionResult.getInt(k * 8)
                if (alphabetIndex in 0..alphabets.length - 1)
                    recognizedText = recognizedText + alphabets[alphabetIndex]
            }
            Log.d("Recognition result:", recognizedText)
        }
        return bitmapWithBoundingBoxes
    }

    fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(
            (128),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt()
        )
    }

    override fun close() {
        detectionInterpreter.close()
        recognitionInterpreter.close()
    }

    companion object {
        public const val TAG = "TfLiteOCRDemo"
        private const val textDetectionModel = "ocr.tflite"
        private const val textRecognitionModel = "ocr.tflite"
        private const val numberThreads = 4
        private const val alphabets = "0123456789abcdefghijklmnopqrstuvwxyz"
        private const val displayImageSize = 257
        private const val detectionImageHeight = 320
        private const val detectionImageWidth = 320
        private val detectionImageMeans =
            floatArrayOf(103.94.toFloat(), 116.78.toFloat(), 123.68.toFloat())
        private val detectionImageStds = floatArrayOf(1.toFloat(), 1.toFloat(), 1.toFloat())
        private val detectionOutputNumRows = 80
        private val detectionOutputNumCols = 80
        private val detectionConfidenceThreshold = 0.5
        private val detectionNMSThreshold = 0.4
        private const val recognitionImageHeight = 31
        private const val recognitionImageWidth = 200
        private const val recognitionImageMean = 0.toFloat()
        private const val recognitionImageStd = 255.toFloat()
        private const val recognitionModelOutputSize = 48
    }

}*/