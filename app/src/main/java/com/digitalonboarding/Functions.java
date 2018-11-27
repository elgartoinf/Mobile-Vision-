package com.digitalonboarding;


import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.vision.face.Face;

import java.io.File;

/**
 * Esta clase es utilizada como herramienta de traspaso de objetos entre actividades
 * esta clase aunque es estatica solo es visible para el paquete local
 */

public class Functions {
    //est variable guarda la ultima rotacion que tuvo la camara cuando se tomo la foto
    static int rotation= 0;
    //ultima cara detectada
    static Face finalFace= null;
    //variable para saber cuando se debe mostrar la cámara delantera
    static boolean faceFront;
    //esta variable permite saber cuando debe ejecutarse la camara en primera instancia
    static boolean showCamera = false;
    //photo capturada por la camara
    static File photo;
    //rostro del paso 1
    public static BitmapDrawable bitmap1;
    //rostro del paso 2
    public static BitmapDrawable bitmap2;
    //variable para saber cuando ejecutar la cámara en primera instancia
    static boolean showCameraDetectFace=true;
    //pais seleccionado
    static String country = "";
    //Variables que almacenan el rostro y el codigo de barras
    //de la actividad multitracker
    public static String barcode;
    public static Face face;
    public static String name = "Andrew";

    public static int selected_library = 0;
    public static int selected_country_position = 0;
}
