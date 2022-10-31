package org.tensorflow.codelabs.objectdetection

import android.app.Activity
import android.app.AlertDialog

class dialogoCargar(val activity:Activity) {
    private lateinit var dialogo:AlertDialog
    fun empezarCarga(){

        /**set Vista*/
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_loading,null)

        /**set Dialogo*/
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialogo = builder.create()
        dialogo.show()
    }

    fun terminarCarga(){
        dialogo.dismiss()
    }
}