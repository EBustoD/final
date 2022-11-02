package org.tensorflow.codelabs.objectdetection


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.File
import java.nio.file.Files


class visualizar_datos_seleccionados: AppCompatActivity(), View.OnClickListener {


    private lateinit var preferencias : SharedPreferences
    private lateinit var imagenSerie: ImageView
    private lateinit var txtLecturaNumeroSerie: TextView
    private lateinit var imagenConsu: ImageView
    private lateinit var txtLecturaConsu: TextView
    private lateinit var txtEditadoSeri: TextView
    private lateinit var txtEditadoConsumo: TextView
    private lateinit var btnAtras: Button
    private lateinit var btnEliminar: Button
    private lateinit var rutaImgConsumo: Uri
    private lateinit var rutaImgNS: Uri
    private lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_dato_seleccionado)

        //configurar controles
        btnAtras = findViewById(R.id.btnBack)
        btnEliminar = findViewById(R.id.btnBorrar)
        btnAtras.setOnClickListener(this)
        btnEliminar.setOnClickListener(this)
        imagenSerie = findViewById(R.id.imageViewRecorteSerie);
        imagenConsu = findViewById(R.id.imageViewRecorteConsumo);
        txtLecturaNumeroSerie = findViewById(R.id.txtLecturaNumeroSerie);
        txtLecturaConsu = findViewById(R.id.txtLecturaConsumo);
        txtEditadoConsumo = findViewById(R.id.txteditadoConsumo)
        txtEditadoSeri = findViewById(R.id.txteditadoSerie)
        visualizadoDato()

        //preferencias para idioma
        val preferencias = getSharedPreferences("idiomas", MODE_PRIVATE)
        val idioma = preferencias.getString("idioma_set","")
        if(idioma == "EU"){
            context = LocaleHelper.setLocale(this, "eu");

        }else{
            context = LocaleHelper.setLocale(this, "es");
        }

        //asignar textos
        txtLecturaNumeroSerie.setHint(context.getResources().getString(R.string.numerSerie))
        txtLecturaConsu.setHint(context.getResources().getString(R.string.consumo))
        btnEliminar.setText(context.getResources().getString(R.string.btn_borrar))
    }

    fun visualizadoDato(){

        preferencias = getSharedPreferences("datos", MODE_PRIVATE)
        val numeroSerie = preferencias.getString("numeroSerie","");
        val serieEditada = preferencias.getString("EDM_SN","");
        rutaImgNS = preferencias.getString("rutaImgNumeroSerie","")?.toUri()!!;
        val consumo = preferencias.getString("consumo","");
        val consumoEditado = preferencias.getString("EDM_CN","");
        rutaImgConsumo = preferencias.getString("rutaImgConsumo","")?.toUri()!!;

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
        if(consumoEditado == "true"){
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

            R.id.btnBorrar->{
                try {
                    showtDialog( context.getResources().getString(R.string.mesaje_borrado) ,context.getResources().getString(R.string.cuidado))

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
        startActivity(Intent(this,visualizar_datos_almacenados::class.java))
    }

    private fun borrarDato(){

        preferencias = getSharedPreferences("datos", MODE_PRIVATE)

        val firmaFichero = preferencias.getString("selectedItem","");

        //Borramos fichero consumo
        val dirPathConsumo = File(getExternalFilesDir(Environment.DIRECTORY_DCIM).toString()+ File.separator.toString() + firmaFichero + File.separator.toString()  + "consumo.txt")
        dirPathConsumo.delete()
        //Borramos fichero numero serie
        val dirPathSerie = File(getExternalFilesDir(Environment.DIRECTORY_DCIM).toString()+ File.separator.toString() + firmaFichero + File.separator.toString()  + "numeroSerie.txt")
        dirPathSerie.delete()


        //Borramos Ruta local
        /*val dirImgNumeroSerie = File(rutaImgNS.toString())
        dirImgNumeroSerie.delete()
        val dirImgConsumo = File(rutaImgConsumo.toString())
        dirImgConsumo.delete()*/
        //Borramos Ruta DCIM
        val dirPathImgConsu = File(getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + File.separator.toString() + firmaFichero  + File.separator.toString() + "consumo_"  + firmaFichero + ".jpg")
        val dirPathImgSerie = File(getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + File.separator.toString() + firmaFichero + File.separator.toString() + "serie_"  + firmaFichero + ".jpg")
        dirPathImgConsu.delete()
        dirPathImgSerie.delete()

        //Borramos la carpeta
        val dirPath = File(getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + firmaFichero + File.separator.toString())
        dirPath.delete()


        navBack()
    }

    private fun showtDialog(mensage: String, titulo:String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            setTitle(titulo)
            setMessage(mensage)
            setPositiveButton(context.getResources().getString(R.string.btn_aceptar)) { _, _ ->
                borrarDato()
            }
            setNegativeButton(context.getResources().getString(R.string.btn_cancelar)) { _, _ ->

            }

        }.create().show()
    }


}

