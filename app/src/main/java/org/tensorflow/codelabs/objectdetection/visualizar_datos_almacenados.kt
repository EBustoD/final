package org.tensorflow.codelabs.objectdetection

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class visualizar_datos_almacenados: AppCompatActivity(), View.OnClickListener {
    private lateinit var listViewLecturas: ListView
    private lateinit var txtLecturaNumeroSerie: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnAtras: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_datos_almacenados)

        //Set el onclick item de la lista
        listViewLecturas = findViewById(R.id.lecturasList)
        listViewLecturas.setClickable(true);
        btnAtras = findViewById(R.id.btnBack)
        btnAtras.setOnClickListener(this)
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
                var list = inFile.list()

                //esto quiere decir que existen tanto el fichero de numero serie y consumo
                if(list.size > 1){
                    lecturas.add(inFile.name)
                }else if (list.size == 1){
                    File(inFile.toString() + File.separator.toString() + list.get(0)).delete()
                    inFile.delete()
                }else{
                    inFile.delete()
                }
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

        //Guardamos el fichero seleccionado

        editor.putString("selectedItem",selectedItem)

        //leemos fichero numSerie
        var dirPath = filesDir.absolutePath + File.separator.toString() + selectedItem
        var text = File(dirPath, "numeroSerie.txt").readText().toString()
        //Guardar datos numero serie en preferencias compartidas
        var editadoManualSR = ""
        if(text.length > 12){
            editadoManualSR = text.subSequence(13,text.length).toString()
        }
        var textOriginal = text.subSequence(0,12)

        //escribirmos en preferencias compartidas
        editor.putString("numeroSerie",textOriginal.toString())

        if (editadoManualSR.length > 0){
            editor.putString("EDM_SN","true")
        }else{
            editor.putString("EDM_SN","false")
        }

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

        var editadoManualCN= ""
        if(text.length > 4){
            editadoManualCN = textConsu.subSequence(5,textConsu.length).toString()
        }
        var textOriginalCN = textConsu.subSequence(0,4)

        editor.putString("consumo",textOriginalCN.toString() )

        if (editadoManualCN.length > 0){
            editor.putString("EDM_CN","true")
        }else{
            editor.putString("EDM_CN","false")
        }


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



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack->{
                try {
                    navBack()

                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }
        }
    }
    override fun onBackPressed() {
        navBack() // optional depending on your needs
    }

    private fun navBack(){
        startActivity(Intent(this,MainActivity::class.java))
    }


}

