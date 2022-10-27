package org.tensorflow.codelabs.objectdetection

class LecturaCompleta {

    var consumo: String
    var numeroSerie: String

    constructor(consumo:String, numeroSerie:String){
        this.consumo = consumo
        this.numeroSerie = numeroSerie
    }

    fun getConsumos(): String {
        return this.consumo
    }

    fun getNSerie(): String {
        return this.consumo
    }
}
