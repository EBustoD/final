package org.tensorflow.codelabs.objectdetection


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.File


class visualizar_datos_seleccionados: AppCompatActivity() {
    private lateinit var imagenSerie: ImageView
    private lateinit var txtLecturaNumeroSerie: TextView
    private lateinit var imagenConsu: ImageView
    private lateinit var txtLecturaConsu: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_dato_seleccionado)

        //configurar controles
        imagenSerie = findViewById(R.id.imageViewRecorteSerie);
        imagenConsu = findViewById(R.id.imageViewRecorteConsumo);
        txtLecturaNumeroSerie = findViewById(R.id.txtLecturaNumeroSerie);
        txtLecturaConsu = findViewById(R.id.txtLecturaConsumo);
        visualizadoDato()

    }

    fun visualizadoDato(){
        val preferencias = getSharedPreferences("datos", MODE_PRIVATE)
        val numeroSerie = preferencias.getString("numeroSerie","");
        val rutaImgNS = preferencias.getString("rutaImgNumeroSerie","")?.toUri();
        val consumo = preferencias.getString("consumo","");
        val rutaImgConsumo= preferencias.getString("rutaImgConsumo","")?.toUri();

        txtLecturaNumeroSerie.setText(numeroSerie);
        imagenSerie.setImageURI(rutaImgNS)

        txtLecturaConsu.setText(consumo)
        imagenConsu.setImageURI(rutaImgConsumo)
    }






}

