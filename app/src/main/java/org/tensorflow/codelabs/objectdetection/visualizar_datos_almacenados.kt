package org.tensorflow.codelabs.objectdetection

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class visualizar_datos_almacenados: AppCompatActivity() {
    private lateinit var listViewLecturas: ListView
    private lateinit var txtLecturaNumeroSerie: com.google.android.material.textfield.TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_datos_almacenados)

        //Set el onclick item de la lista
        listViewLecturas = findViewById(R.id.lecturasList)
        listViewLecturas.setClickable(true);
        //txtLecturaNumeroSerie = findViewById(R.id.text)

        listViewLecturas.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            //Toast.makeText(this, selectedItem , Toast.LENGTH_SHORT).show()
            navToVisualizadoDato(selectedItem)
        }
        //MOSTRAMOS TODAS LAS CARPETAS QUE ALMACENAN LAS LECTURAS.
        // use arrayadapter and define an array
        val arrayAdapter: ArrayAdapter<*>
        val lecturas = mutableListOf<String>()
        val f: File = File(filesDir.absolutePath)
        val files = f.listFiles()
        for (inFile in files) {
            if (inFile.isDirectory) {
                // is directory
                //users.plus(inFile.name)
                lecturas.add(inFile.name)
            }
        }
        // access the listView from xml file
        //var mListView = findViewById<ListView>(R.id.lecturasList)
        //cambio el orden para que las lecturas nuevas salgan las primeras en el ListView
        lecturas.reverse()
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, lecturas)
        listViewLecturas.adapter = arrayAdapter
    }

    fun navToVisualizadoDato(selectedItem : String){

        //Variables para preferencias compartidas
        val preferencias = getSharedPreferences("datos", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferencias.edit()

        //leemos fichero numSerie
        var dirPath = filesDir.absolutePath + File.separator.toString() + selectedItem
        var text = File(dirPath, "numeroSerie.txt").readText().toString()
        //Guardar datos numero serie en preferencias compartidas
        editor.putString("numeroSerie",text)
        editor.commit()
        finish()
        //leemos foto numSerie //Guardar datos foto numero serie en preferencias compartidas.
        ///data/data/org.tensorflow.codelabs.objectdetection/app_imagenesLectura
        var dirPathImgSerie = "data/data/" +  "org.tensorflow.codelabs.objectdetection" + File.separator.toString() + "app_imagenesLectura" +  File.separator.toString() + "serie_" + selectedItem + ".jpg"
        editor.putString("rutaImgNumeroSerie",dirPathImgSerie)
        editor.commit()
        finish()


        //LO MISMO CON EL CONSUMO Y CON LA IMAGEN (guardar ruta de la imagen y luego mostrarla en el view)

        //leemos fichero consumo
        var dirPathConsu = filesDir.absolutePath + File.separator.toString() + selectedItem
        var textConsu = File(dirPathConsu, "consumo.txt").readText().toString()
        //Guardar datos numero serie en preferencias compartidas
        editor.putString("consumo",textConsu )
        editor.commit()
        finish()
        //leemos foto consumo //Guardar datos foto numero serie en preferencias compartidas.
        ///data/data/org.tensorflow.codelabs.objectdetection/app_imagenesLectura
        var dirPathImgConsu = "data/data/" +  "org.tensorflow.codelabs.objectdetection" + File.separator.toString() + "app_imagenesLectura" +  File.separator.toString() + "consumo_" + selectedItem + ".jpg"
        editor.putString("rutaImgConsumo",dirPathImgConsu)
        editor.commit()
        finish()


        //Navegar a la vista detalle
        navegar()
    }

    fun navegar(){
        startActivity(Intent(this,visualizar_datos_seleccionados::class.java))
    }

    fun imageReaderNew(root: File) {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".jpeg")) {
                    // File absolute path
                    Log.e("downloadFilePath", currentFile.getAbsolutePath())
                    // File Name
                    Log.e("downloadFileName", currentFile.getName())
                    fileList.add(currentFile.absoluteFile)
                }
            }
            Log.w("fileList", "" + fileList.size)
        }
    }



}

