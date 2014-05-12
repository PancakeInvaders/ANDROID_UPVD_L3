package org.opencv.samples.imagemanipulations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;



import com.googlecode.tesseract.android.TessBaseAPI;

public class LectureIMG {
	
	File path;
	File pathtess;
	
	public final String lang = "eng";


	
	public LectureIMG(AssetManager assetManager) {
	
		path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		
		pathtess = new File(path.getAbsolutePath() + "/tessdata");

	
			String[] paths = new String[] { path.getAbsolutePath(), pathtess.getAbsolutePath() };
	
			for (String path : paths) {
				File dir = new File(path);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						System.out.println("ERROR: Creation of directory " + path + " on sdcard failed");
						return;
					} else {
						System.out.println( "Created directory " + path + " on sdcard");
					}
				}
	
			}
			
			// lang.traineddata file with the app (in assets folder)
			// You can get them at:
			// http://code.google.com/p/tesseract-ocr/downloads/list
			// This area needs work and optimization
			if (!(new File(path + "/tessdata/" + lang + ".traineddata")).exists()) {
				try {
	
					
					InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
					//GZIPInputStream gin = new GZIPInputStream(in);
					//new File(path + "/tessdata/" + lang + ".traineddata").createNewFile();
					OutputStream out = new FileOutputStream(path
							+ "/tessdata/" + lang + ".traineddata");
	
					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					//while ((lenf = gin.read(buff)) > 0) {
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					//gin.close();
					out.close();
					
					System.out.println( "Copied " + lang + " traineddata");
				} catch (IOException e) {
					System.out.println( "Was unable to copy " + lang + " traineddata " + e.toString());
					e.printStackTrace(System.out);
				}
			}
		
		
		
		
	
	}
	
	
	
	public String lireTextFromImage(Mat img){
		
		// Je part du principe que cette fonction sert à lire du texte (ou un nombre) dans une image 
		
		// en cas de de reussite elle renvoie une renvoie le texte lu
		
		// en cas d'echec elle renvoie la chaine de caractères "echec lecture"

		String filename = "temporary_file.png";
		File file = new File(pathtess, filename);
		filename = file.toString();
		Highgui.imwrite(filename, img);
		
		System.out.println(path.getAbsolutePath());
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		if(pathtess.exists() == false){
			
			boolean result = pathtess.mkdir();
			
			if(result == true){
			
				System.out.println("dir created");
				
			}
			
		}
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(path.getAbsolutePath(), lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		System.out.println(recognizedText);
		
		boolean parsable = true;
		
		try{
			
			Integer.parseInt(recognizedText);
			
		}
		catch(NumberFormatException e ){
			
			parsable = false;
			
		}
		
		if(parsable == true){
			
			return recognizedText;
			
		}
		else{
			
			return "echec lecture";
		
		}
		
	}

}
