package org.opencv.samples.imagemanipulations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;



import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Reconnaisance de caractere d'une image
 * @author CAMPOY - CANO
 */
public class LectureIMG {

	/**
	 * Attributs
	 */
	File path;
	File pathtess;
	public final String lang = "eng";

	/**
	 * Constructeur surchargé
	 * @param assetManager
	 */
	public LectureIMG(AssetManager assetManager) {
		// Chemin du répertoire image du téléphone
		path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		
		// Création du fichier avec son chemin
		pathtess = new File(path.getAbsolutePath() + "/tessdata");

		// Tableau de chemin
		String[] paths = new String[] { path.getAbsolutePath(), pathtess.getAbsolutePath() };

		// Parcour des chemins
		for (String path : paths) {
			File dir = new File(path);
			// Si le dossier n'existe pas
			if (!dir.exists()) {
				// Si la création du dossier echoue
				if (!dir.mkdirs()) {
					System.out.println("ERREUR: Creation du dossier " + path + " sur le stockage a echouee.");
					return;
				} else {
					System.out.println( "Création du dossier " + path + " sur la carte sd.");
				}
			}

		}
		
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// Optimisation possible ?
		// Si le fichier n'existe pas deja
		if (!(new File(path + "/tessdata/" + lang + ".traineddata")).exists()) {
			try {
				//
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				OutputStream out = new FileOutputStream(path + "/tessdata/" + lang + ".traineddata");

				byte[] buf = new byte[1024];
				int len;

				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();

				out.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
	}
	
	
	/**
	 * OCR : Reconnaissance de caractere 
	 * A partir d'une matrice image
	 * @param img
	 * @return String
	 */
	public String lireTextFromImage(Mat img){
		/*
		 * Je part du principe que cette fonction sert à lire du texte (ou un nombre) dans une image
		 * en cas de de reussite elle renvoie une renvoie le texte lu
		 * en cas d'echec elle renvoie la chaine de caractères "echec lecture"
		 */
		
		// Nom du fichier
		String filename = "temporary_file.png";
		// Nouveau fichier
		File file = new File(pathtess, filename);
		//
		filename = file.toString();
		Highgui.imwrite(filename, img);
		
		// Crée un objet BitmalFactory
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		// Decode le fichier en bitmap
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		// Copie le bitmap avec manipulation
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		// Si pathtess n'existe pas
		if(pathtess.exists() == false){
			// Crée le dossier
			boolean result = pathtess.mkdir();
			// Si le dossier est crée
			if(result == true){
				// Message 
				System.out.println("dir created");
			}
		}

		/*
		 * Reconnaissance de caractère
		 * Utilisation de Tesseract
		 */
		// Crée un objet de type TessBaseAPI et l'initialise
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(path.getAbsolutePath(), lang);
		baseApi.setImage(bitmap);
		baseApi.setVariable("tessedit_char_whitelist", "123456789"); // Definie le type de variable (caractère autorisé)
		
		// Recupere dans une chaine le caractère retourné
		String recognizedText = baseApi.getUTF8Text();
		
		// Bool qui indique si le text a pu etre convertie 
		boolean parsable = true;
		// Variable qui recupere le chiffre
		int num = 0;

		// Parse le caractere
		try{
			num = Integer.parseInt(recognizedText);
		}
		catch(NumberFormatException e ){
			parsable = false;
		}
		
		// Si le caractere a ete parse
		if(parsable == true){
			// Si le chiffre est inférieur a 1 et superieur a 9
			if((num < 1) || (num > 9)){
				return "echec lecture";
			}
			
			// Renvoie la reconnaisance du texte
			return recognizedText;
		}
		else {
			return "echec lecture";
		}
	}
}
