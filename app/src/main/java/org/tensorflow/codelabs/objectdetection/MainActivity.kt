package org.tensorflow.codelabs.objectdetection

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var bntScanner: Button
    private lateinit var btnAlmacen: Button
    private lateinit var btnIdioma: Button
    private lateinit var context : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Configuracion botones navegacion
        bntScanner = findViewById(R.id.btnScanner)
        btnAlmacen = findViewById(R.id.btnAlmacen)
        btnIdioma = findViewById(R.id.btnIdioma)
        btnIdioma.setOnClickListener(this)
        bntScanner.setOnClickListener(this)
        btnAlmacen.setOnClickListener(this)

        val preferencias = getSharedPreferences("idiomas", MODE_PRIVATE)
        val idioma = preferencias.getString("idioma_set","")
        if(idioma == "EU"){
            context = LocaleHelper.setLocale(this, "eu");
        }else{
            context = LocaleHelper.setLocale(this, "es")
        }

        var resources = context.getResources();
        btnAlmacen.setText(resources.getString(R.string.btn_datos_almacen));
        btnIdioma.setText(resources.getString(R.string.btn_idioma));
        bntScanner.setText(resources.getString(R.string.btn_start_scan))

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            //Iniciar Scanner
            R.id.btnScanner-> {
                try {
                    val preferencias = getSharedPreferences("idiomas", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferencias.edit()
                    if(btnIdioma.getText() == "EU"){
                        editor.putString("idioma_set","ES")
                        editor.commit()

                    }else{
                        editor.putString("idioma_set","EU")
                        editor.commit()

                    }
                    startActivity(Intent(this,scanner_numSerie::class.java))
                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }

            //alamacen
            R.id.btnAlmacen -> {
                try {
                    startActivity(Intent(this,visualizar_datos_almacenados::class.java))
                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }

            //cambioIdioma
            R.id.btnIdioma-> {

                try {
                    val preferencias = getSharedPreferences("idiomas", MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferencias.edit()
                    if(btnIdioma.getText() == "EU"){
                        context = LocaleHelper.setLocale(this, "eu");
                        editor.putString("idioma_set","EU")
                        editor.commit()

                    }else{
                        context = LocaleHelper.setLocale(this, "es");
                        editor.putString("idioma_set","ES")
                        editor.commit()
                    }


                    var resources = context.getResources();
                    btnAlmacen.setText(resources.getString(R.string.btn_datos_almacen));
                    btnIdioma.setText(resources.getString(R.string.btn_idioma));
                    bntScanner.setText(resources.getString(R.string.btn_start_scan))

                } catch (e: ActivityNotFoundException) {
                    Log.e(scanner_numSerie.TAG, e.message.toString())
                }
            }

        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))
    }




}