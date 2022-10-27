package org.tensorflow.codelabs.objectdetection


import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.File


class visualizar_datos_seleccionados: AppCompatActivity(), View.OnClickListener {
    private lateinit var imagenSerie: ImageView
    private lateinit var txtLecturaNumeroSerie: TextView
    private lateinit var imagenConsu: ImageView
    private lateinit var txtLecturaConsu: TextView
    private lateinit var txtEditadoSeri: TextView
    private lateinit var txtEditadoConsumo: TextView
    private lateinit var btnAtras: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_dato_seleccionado)

        //configurar controles
        btnAtras = findViewById(R.id.btnBack)
        btnAtras.setOnClickListener(this)
        imagenSerie = findViewById(R.id.imageViewRecorteSerie);
        imagenConsu = findViewById(R.id.imageViewRecorteConsumo);
        txtLecturaNumeroSerie = findViewById(R.id.txtLecturaNumeroSerie);
        txtLecturaConsu = findViewById(R.id.txtLecturaConsumo);
        txtEditadoConsumo = findViewById(R.id.txteditadoConsumo)
        txtEditadoSeri = findViewById(R.id.txteditadoSerie)
        visualizadoDato()

    }

    fun visualizadoDato(){
        val preferencias = getSharedPreferences("datos", MODE_PRIVATE)
        val numeroSerie = preferencias.getString("numeroSerie","");
        val serieEditada = preferencias.getString("EDM_SN","");
        val rutaImgNS = preferencias.getString("rutaImgNumeroSerie","")?.toUri();
        val consumo = preferencias.getString("consumo","");
        val consumoEditado = preferencias.getString("EDM_CN","");
        val rutaImgConsumo= preferencias.getString("rutaImgConsumo","")?.toUri();

        txtLecturaNumeroSerie.setText(numeroSerie);
        imagenSerie.setImageURI(rutaImgNS)
        //Marcamos como editado a mano o escaneado
        if(serieEditada == "true"){
            txtEditadoSeri.setText("EDM")
        }else{
            txtEditadoSeri.setText("SC")
        }

        txtLecturaConsu.setText(consumo)
        imagenConsu.setImageURI(rutaImgConsumo)
        //Marcamos como editado a mano o escaneado
        if(serieEditada == "true"){
            txtEditadoConsumo.setText("EDM")
        }else{
            txtEditadoConsumo.setText("SC")
        }
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

    private fun navBack(){
        startActivity(Intent(this,visualizar_datos_almacenados::class.java))
    }


}

