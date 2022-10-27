package org.tensorflow.codelabs.objectdetection

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var bntScanner: Button
    private lateinit var btnAlmacen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Configuracion botones navegacion

        bntScanner = findViewById(R.id.btnScanner)
        btnAlmacen = findViewById(R.id.btnAlmacen)

        bntScanner.setOnClickListener(this)
        btnAlmacen.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {

            //Iniciar Scanner
            R.id.btnScanner-> {
                try {
                    startActivity(Intent(this,scanner_numSerie::class.java))
                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }

            //editar Texto
            R.id.btnAlmacen -> {
                try {
                    startActivity(Intent(this,visualizar_datos_almacenados::class.java))
                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }
        }
    }


}