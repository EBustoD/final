/**
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.codelabs.objectdetection

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min


class scanner_consumo : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TAG = "TFLite - ODT"
        const val REQUEST_IMAGE_CAPTURE: Int = 1
        private const val MAX_FONT_SIZE = 96F
    }

    /*private var ocrModel: OCRTextModelExecutor? = null*/
    var rutaImagen = ""
    var timeStamp = ""
    var editado = false
    var bitMapCaptura: Bitmap? = null
    private lateinit var intentAux: Intent
    private lateinit var captureImageFab: Button
    private lateinit var btnConfirmar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnAtras: Button
    private lateinit var inputImageView: ImageView
    private lateinit var outputImageView: ImageView
    private lateinit var txtLecturaConsumo: com.google.android.material.textfield.TextInputEditText
    private lateinit var tvPlaceholder: TextView
    private lateinit var t: TextView
    private lateinit var currentPhotoPath: String
    private lateinit var dato: String
    private lateinit var context: Context
    private val cargando = dialogoCargar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_consumo)
        //Configuracion botones
        captureImageFab = findViewById(R.id.captureImageFab)
        btnEditar = findViewById(R.id.btnEdit)
        btnAtras = findViewById(R.id.btnBack)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        captureImageFab.setOnClickListener(this)
        btnEditar.setOnClickListener(this)
        btnConfirmar.setOnClickListener(this)
        btnAtras.setOnClickListener(this)
        //configuracion del textView
        txtLecturaConsumo = findViewById(R.id.txtLecturaConsumo)
        //textoOCR = findViewById(R.id.textViewOCR)
        inputImageView = findViewById(R.id.imageViewRecorte)
        //imagenView
        outputImageView = findViewById(R.id.imageViewRecorte)

        this.requestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //preferencias para idioma
        val preferencias = getSharedPreferences("idiomas", MODE_PRIVATE)
        val idioma = preferencias.getString("idioma_set", "")
        if (idioma == "EU") {
            context = LocaleHelper.setLocale(this, "eu");

        } else {
            context = LocaleHelper.setLocale(this, "es");

        }
        //asignar textos
        btnConfirmar.setText(context.getResources().getString(R.string.btn_confirmar))
        captureImageFab.setText(context.getResources().getString(R.string.btn_escanear))
        txtLecturaConsumo.setHint(context.getResources().getString(R.string.consumo))

        //Funcion de sacar el Progress dialog

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK
        ) {
            setViewAndDetect(getCapturedImage())
        }
    }

    /**
     * onClick(v: View?)
     *      Detect touches on the UI components
     */
    //Definimos los onClick
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {

            //capturar imagen
            R.id.captureImageFab -> {
                try {
                    dispatchTakePictureIntent()
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message.toString())
                }
            }

            //confirmar Texto
            R.id.btnConfirmar -> {
                try {
                    confirmarDato()

                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message.toString())
                }
            }

            R.id.btnEdit -> {
                try {
                    activarEdicion()

                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }

            R.id.btnBack -> {
                try {
                    navBack()

                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }
        }
    }

    /**
     * runObjectDetection(bitmap: Bitmap)
     *      TFLite Object Detection function
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        // Step 1: Create TFLite's TensorImage object
        val image = TensorImage.fromBitmap(bitmap)

        // Step 2: Initialize the detector object
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.2f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            this,
            "contador.tflite",
            options
        )

        // Step 3: Feed given image to the detector
        val results = detector.detect(image)

        // Step 4: Parse the detection result and show it
        val resultToDisplay = results.map {
            // Get the top-1 category and craft the display text
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"

            // Create a data object to display the detection result
            DetectionResult(it.boundingBox, text)
        }
        // Pintar la deteccion en un bitmap y mostrarlo
        drawDetectionResult(bitmap, resultToDisplay)
    }


    /**
     * setViewAndDetect(bitmap: Bitmap)
     *      Set image to view and call object detection
     */
    private fun setViewAndDetect(bitmap: Bitmap) {
        // Display capture image
        inputImageView.setImageBitmap(bitmap)
        //tvPlaceholder.visibility = View.INVISIBLE

        // Run ODT and display result
        // Note that we run this in the background thread to avoid blocking the app UI because
        // TFLite object detection is a synchronised process.
        lifecycleScope.launch(Dispatchers.Default) { runObjectDetection(bitmap) }
        cargando.empezarCarga()
    }

    /**
     * getCapturedImage():
     *      Decodes and crops the captured image from camera.
     */
    private fun getCapturedImage(): Bitmap {
        // Get the dimensions of the View
        val targetW: Int = inputImageView.width
        val targetH: Int = inputImageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inMutable = true
        }
        val exifInterface = ExifInterface(currentPhotoPath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        bitMapCaptura = bitmap;
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, 270f)
            }
            else -> {
                bitmap
            }
        }
    }


    /**
     * rotateImage():
     *     Decodes and crops the captured image from camera.
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    /**
     * createImageFile():
     *     Generamos una imagen temporal para que la camara escriba sobre ella
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val current = LocalDateTime.now().toString() //fecha y hora actual
        // var dirArchivoFt = File(filesDir.absolutePath + File.separator.toString() +"_"+ current)

        // Create an image file name
        //timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val preferencias = getSharedPreferences("datosTimeStamp", MODE_PRIVATE)
        timeStamp = preferencias.getString("timeStamp", "").toString();

        val storageDir: File? =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator.toString())
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
            //dirArchivoFt
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * dispatchTakePictureIntent():
     *     Start the Camera app to take a photo.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (e: IOException) {
                    Log.e(TAG, e.message.toString())
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    var photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "org.tensorflow.codelabs.objectdetection.fileprovider",
                        it
                    )

                    rutaImagen = photoURI.toString()

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    /**
     * drawDetectionResult(bitmap: Bitmap, detectionResults: List<DetectionResult>
     *      Draw a box around each objects and show the object's name.
     */
    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<DetectionResult>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        detectionResults.forEach {

            val left = it.boundingBox.left.toInt()
            val top = it.boundingBox.top.toInt()
            val right = it.boundingBox.right.toInt()
            val bottom = it.boundingBox.bottom.toInt()

            Log.d("Izq", left.toString())
            Log.d("Arriba", top.toString())
            Log.d("Der", right.toString())
            Log.d("Abajo", bottom.toString())
            Log.d("ancho", it.boundingBox.width().toString())
            Log.d("caja", it.boundingBox.toString())

            val bitmapSalida = Bitmap.createBitmap(
                bitmap,
                it.boundingBox.left.toInt(),
                it.boundingBox.top.toInt(),
                it.boundingBox.width().toInt(),
                it.boundingBox.height().toInt()
            )

            //Se pasa a el detector de digitos
            runDigitDetection(bitmapSalida)
        }
        return outputBitmap
    }


    //guardaremos el dato en un en el fichero local y navegamos a la siguiente vista
    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmarDato() {

        //Escribir en el fichero
        try {
            var consumo = txtLecturaConsumo.text.toString()
            if (bitMapCaptura === null) {
                Toast.makeText(
                    this,
                    context.getResources().getString(R.string.ErrorImagen),
                    Toast.LENGTH_LONG
                ).show()
            } else if (consumo.length == 0) {
                Toast.makeText(
                    this,
                    context.getResources().getString(R.string.ErrorConsumoVacio),
                    Toast.LENGTH_LONG
                ).show()
            } else {

                val dirPath =
                    getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + File.separator.toString() + timeStamp
                createDirectorio(dirPath)

                //escribimos el numero, si esta editado lo marcamos
                if (editado) {
                    consumo += "_EDM"
                    editado = false
                }
                File(dirPath, "consumo.txt").printWriter()
                    .use { out -> out.println(consumo) } //se escribe el numero en el fichero
                var name = "consumo_" + timeStamp
                saveToInternalStorage(name, bitMapCaptura, this)
                Toast.makeText(
                    this,
                    context.getResources().getString(R.string.guardarExito),
                    Toast.LENGTH_SHORT
                ).show()
                //navegar a la siguiente vista
                navegar()
            }
        } catch (e: IOException) {
            Toast.makeText(
                this,
                context.getResources().getString(R.string.ErrorGeneral),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun navegar() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun createDirectorio(dirPath: String) {
        val projDir = File(dirPath)
        if (!projDir.exists()) {
            projDir.mkdirs()
        }
    }

    private fun saveToInternalStorage(
        name: String,
        bitmapImage: Bitmap?,
        context: Context
    ): String {
        val cw = ContextWrapper(context.applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        //val directory = cw.getDir("imagenesLectura", Context.MODE_PRIVATE)
        val dirPath =
            getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + File.separator.toString() + timeStamp
        // Create imageDir
        // Log.d("PATH", directory.path)
        val mypath = File(dirPath, name + ".jpg")
        val mypathAux = File(dirPath, name + ".jpg")
        var fos: FileOutputStream? = null
        try {
            //fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            //bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos = FileOutputStream(mypathAux)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //return directory.absolutePath
        return dirPath
    }
    //Detectar numeros en el recorte del contador

    /**
     * runDigitDetection(bitmap: Bitmap)
     *      TFLite Object Detection function
     *      Detecta digitos en el recorte del contador y pasa esas detecciones a una funcion para crear el string
     */
    private fun runDigitDetection(bitmap: Bitmap) {
        // Step 1: Create TFLite's TensorImage object
        val image = TensorImage.fromBitmap(bitmap)

        // Step 2: Initialize the detector object
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.2f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            this,
            "digitos.tflite",
            options
        )
        // Step 3: Feed given image to the detector
        val results = detector.detect(image)
        // Step 4: Parse the detection result and show it
        val resultToDisplay = results.map {
            // Get the top-1 category and craft the display text
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"
            // Create a data object to display the detection result
            DetectionResultDigitos(it.boundingBox, text)
        }
        // Draw the detection result on the bitmap and show it.
        val bitmapSalida = extraerConsumo(bitmap, resultToDisplay)
        runOnUiThread {
            outputImageView.setImageBitmap(bitmapSalida)
        }
    }

    /**
     * extraerConsumo(detectionResults: List<DetectionResultDigitos>
     *      extrae los numeros de las detecciones del modelo de TFlite y dibuja un recuadro alrededor de cada deteccion
     */
    private fun extraerConsumo(
        bitmap: Bitmap,
        detectionResults: List<DetectionResultDigitos>
    ): Bitmap? {
        //Utiles para editar la imagen y pintar las detecciones encima
        val pen = Paint()
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        //lista en la que se almacenan los numeros detectados
        val sortedValues = mutableListOf(0 to "x")
        sortedValues.removeFirst()
        //recorro la lista de detecciones
        detectionResults.forEach {
            pen.color = Color.rgb(19, 118, 91)
            pen.strokeWidth = 4F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
            canvas.drawRect(box, pen)
            val left = it.boundingBox.left.toInt()
            Log.d("Izq", left.toString())
            Log.d("Numero", it.text)
            //Quito los ultimos 5 caracteres de el label de la deteccion
            var numeroDepurado = it.text.dropLast(5)
            sortedValues.add(left to numeroDepurado)
            Log.d("LISTA", sortedValues.toString())
        }
        //ordeno la lista de pares (coordenada,consumo) por coordenadas
        sortedValues.sortBy { it.first }
        Log.d("LISTAORDENADA", sortedValues.toString())
        var consumo = ""
        //encadeno los numeros del consumo en uno solo
        sortedValues.forEach {
            consumo = consumo.plus(it.second)
        }
        txtLecturaConsumo.setText(consumo)
        cargando.terminarCarga()
        return outputBitmap
    }

    private fun activarEdicion() {
        editado = true
        txtLecturaConsumo.setEnabled(true);
    }

    override fun onBackPressed() {
        navBack() // optional depending on your needs
    }

    private fun navBack() {
        startActivity(Intent(this, scanner_numSerie::class.java))
    }

}

operator fun Int.invoke(screenOrientationPortrait: Int) {

}

/**
 * DetectionResultDigitos
 *      Clase en la que se guardan los digitos detectados
 */
data class DetectionResultDigitos(val boundingBox: RectF, val text: String)




