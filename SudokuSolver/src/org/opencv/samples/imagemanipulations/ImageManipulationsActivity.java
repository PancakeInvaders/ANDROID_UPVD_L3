package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Entr�e principal de l'application
 * @author CAMPOY - CANO
 */
public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {

	/**
	 * Attributs 
	 * Private
	 */
	private static final String  TAG                 = "OCVSample::Activity";
    private MenuItem             mItemVideo;
    private MenuItem             mItemPreviewSudoku;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat                  mIntermediateMat;
    private int                  mHistSizeNum = 25;
    private Mat                  mSepiaKernel;

    
    /**
     * Attributs
     * Public
     */
    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_POSTERIZE = 7;
    public static int           viewMode = VIEW_MODE_RGBA;

    /**
     * Variable Global
     */
    CharSequence text = "Une erreur est survenue lors de l'analyse de l'image, la r�solution du sudoku a �t� anul�e";
    Context context;
	int duration = Toast.LENGTH_LONG;
	double w;
	double h;
	double sc2;
	double sizeCell;
	double w2;
	boolean computingImage = false; // Bool qui indique si oui ou non l'image doit etre calcul�
	boolean threadStopRequest; // Autorise ou Non l'execution du thread
    Mat rgba;
    Size sizeRgba;
    Toast toast;
    LectureIMG lecteur;
    ArrayList<PointValue> listADessiner;
    Thread t;
    
    
    
    /**
     * M�thode Priv�e
     * Thread de ResolveSudoku
     */
    private Runnable r = new Runnable() {
		@Override
		public void run() {
			resolveSudoku();
		}
	};
	
	
	/**
	 * M�thode Priv�e
	 */
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    
    /**
     * 
     */
    public ImageManipulationsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    
    /** 
     * Appel� lorsque l'activit� viens d'etre cr�e. 
     */
    @SuppressLint("ShowToast")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// Cr�e un objet de la Classe LectureIMG
    	lecteur = new LectureIMG(getAssets());
    	
    	// Autorise l'execution du thread de resolution
    	threadStopRequest = false;

    	// Lance le thread
    	t = new Thread(r);
    	
    	// Initialise la couche view
    	setContentView(R.layout.image_manipulations_surface_view);
    	
    	// Prepare le toast
    	context = getApplicationContext();
    	toast = Toast.makeText(context, text, duration);
    	  
    	// Liste des chiffres de la grille a dessiner avec leur coordon�es
    	listADessiner = new ArrayList<PointValue>();
        
    	Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.image_manipulations_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    /**
     * 
     */
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * 
     */
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    /**
     * 
     */
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * M�thode de cr�ation pour le menu de l'application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemVideo  = menu.add("Video");
        mItemPreviewSudoku = menu.add("Sudoku");
        return true;
    }

    /**
     * M�thode d'execution en fonction du choix menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemVideo)
            viewMode = VIEW_MODE_RGBA;
        else if (item == mItemPreviewSudoku)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    /**
     * M�thode d'initialisation de la vue de la camera
     */
    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();

        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    /**
     * Camera arret�
     * D�salocation m�moire de la matrice image
     */
    public void onCameraViewStopped() {
        if (mIntermediateMat != null) {
            mIntermediateMat.release();
        }
        mIntermediateMat = null;
    }

    /**
     * M�thode d'entr�e pour la cam�ra
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // Recupere la matrice Rgba de la camera
    	rgba = inputFrame.rgba();
    	// Recupere la taille de la matrice
        sizeRgba = rgba.size();

        // Switch en fonction du mode s�lectionn�
        switch (ImageManipulationsActivity.viewMode) {
        
        	// Mode RGBA 
	        case ImageManipulationsActivity.VIEW_MODE_RGBA:
	        	// Stop le traitement en cours
	        	if(t.isAlive()) {
	        		// Demande l'arret du thread
	        		threadStopRequest = true;
	        		// Attend que le thread s'arrete
	        		while(t.isAlive() == true){}
	        		// Relance un nouveau thread
	        		t = new Thread(r);
	        		computingImage = false;
	        		// R�initialise la liste des chiffres
	        		listADessiner = new ArrayList<PointValue>();
	        	}
	            break;
	
	        // MODE POSTERIZE
	        case ImageManipulationsActivity.VIEW_MODE_POSTERIZE:
	        	// Autorise l'execution du thread
	        	threadStopRequest = false;
	        	// Calcul la densit�
	        	int thickness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
	            if(thickness > 5) thickness = 3;
	            
	            // Recupere la largeur et hauteur
	            w = sizeRgba.width;
	            h = sizeRgba.height;
	            // Recupere le milieu de la grille
	            w2 = w/2;
	            // Taille d'une cellule
	            sizeCell = h/9;
	            // Recupere le milieu d'une cellule
	            sc2 = sizeCell /2;
	            
	            // Initialise 2 points
	            Point p1;
	            Point p2;
	            
	            /*
	             * Dessine les traits horizontals de la grille
	             */
	            p1 = new Point(w2 - sc2 - 4*sizeCell, 0);
	            p2 = new Point(w2 - sc2 - 4*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w2 - sc2 - 3*sizeCell, 0);
	            p2 = new Point(w2 - sc2 - 3*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            p1 = new Point(w2 - sc2 - 2*sizeCell, 0);
	            p2 = new Point(w2 - sc2 - 2*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            
	            p1 = new Point(w2 - sc2 - 1*sizeCell, 0);
	            p2 = new Point(w2 - sc2 - 1*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w2 - sc2, 0);
	            p2 = new Point(w2 - sc2, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);			
	            p1 = new Point(w2 + sc2, 0);
	            p2 = new Point(w2 + sc2, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            
	            p1 = new Point(w2 + sc2 + 1*sizeCell, 0);
	            p2 = new Point(w2 + sc2 + 1*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w2 + sc2 + 2*sizeCell, 0);
	            p2 = new Point(w2 + sc2 + 2*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            p1 = new Point(w2 + sc2 + 3*sizeCell, 0);
	            p2 = new Point(w2 + sc2 + 3*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            
	            p1 = new Point(w2 + sc2 + 4*sizeCell, 0);
	            p2 = new Point(w2 + sc2 + 4*sizeCell, h);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            
	            /*
	             * Dessine les traits verticaux de la grille
	             */
	            p1 = new Point(w/2 - h/2, h/2 - sc2 - 4*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 - sc2 - 4*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w/2 - h/2, h/2 - sc2 - 3*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 - sc2 - 3*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            p1 = new Point(w/2 - h/2, h/2 - sc2 - 2*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 - sc2 - 2*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            
	            p1 = new Point(w/2 - h/2, h/2 - sc2 - 1*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 - sc2 - 1*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w/2 - h/2, h/2 - sc2);
	            p2 = new Point(w/2 + h/2, h/2 - sc2);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);		
	            p1 = new Point(w/2 - h/2, h/2 + sc2);
	            p2 = new Point(w/2 + h/2, h/2 + sc2);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);		
	            
	            p1 = new Point(w/2 - h/2, h/2 + sc2 + 1*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 + sc2 + 1*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            p1 = new Point(w/2 - h/2, h/2 + sc2 + 2*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 + sc2 + 2*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            p1 = new Point(w/2 - h/2, h/2 + sc2 + 3*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 + sc2 + 3*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);
	            
	            p1 = new Point(w/2 - h/2, h/2 + sc2 + 4*sizeCell);
	            p2 = new Point(w/2 + h/2, h/2 + sc2 + 4*sizeCell);
	            Core.line(rgba, p1, p2, new Scalar(250,250,250), 3*thickness);
	            
		        // On dessine les chiffres de la grille r�solue
	            // Initialise les points x et y
		        double x;
		        double y;
		
		        for(PointValue pv : listADessiner){
		        	// Recupere les coordonn�es pour ecrire
		        	x = pv.getPoint().x;
		        	y = pv.getPoint().y;
		        	
		        	// Ecrit le chiffre sur la grille
		            Core.putText(rgba, "" + pv.getValue(), new Point (x + (sc2/2) - 5, y + sizeCell - (sc2/2)  + 7), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(25,25,25), 10);
		        }
		        
		        // Si l'image doit etre calcul�e alors
		        if(computingImage == true){
		        	// Affiche la chaine d'attente
		        	String s = "Calcul de l'image... Cela peut prendre plusieurs minutes.";
		        	Core.putText(rgba, s, new Point (w/2 - (8.2)*s.length(), h - 50), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255,117,5), 3);
		        }
		        
		        break;
        }
        
        // Renvoi la matrice rgba
        return rgba;
    }
    
    /**
     * M�thode qui execute le thread
     * @param view
     */
    public void resolveSudokuThread(View view){
    	if(t.isAlive() == false){
    		t.start();
    	}
    }
    
    /**
     * M�thode qui r�solu le sudoku (OCR, Fonction de r�solution d'une grille
     */
    public void resolveSudoku() {
    	
    	// Si Mode Posterize
    	if (ImageManipulationsActivity.viewMode == ImageManipulationsActivity.VIEW_MODE_POSTERIZE ){
	 	   	// extraire grille de l'img , puis r�soude la grille, puis corriger l'img
	    	// instantiation de la matrice du sudoku � 0, de cette mani�re si il y a un 0, �a veux dire que c'est pas instanci�
	    	// les chiffres valables �tant compris entre 1 et 9
	    	// pour acceder � une case : matriceSudoku.get(x).get(y), en supposant qu'on a mit le telephone dans sa largeur
	    	// fin de l'instanciation
	    	
	    	// Je part du principe que j'ai une fonction servant � lire un nombre dans une image 
	    	// cette fonction ayant pour signature static String lireTextFromImage(Mat img);
	    	
			// On sauvegarde l'image actuelle pour faire les lectures dessus
			Mat img = rgba.submat(0, (int)h, 0, (int)w); 
	
			// Indique que l'image doit etre calcul�e
			computingImage = true;
	
			// Appel � Looper.prepare dans le but de preparer le thread � l'affichage de Toasts
			Looper.prepare();
	
			// Initialise une variable de type Matrice et String
			Mat imgCell;
			String number;
			
			// X et Y representent le point en haut gauche de la cellule � lire, 
			// on les initialise en haut � gauche de la grille de sudoku
			double x = w/2 - sc2 - 4*sizeCell;
			double y = 0;
	    	
			// Initialisation de la matrice du sudoku
	    	int[][] sudokuMatrice = new int[9][9];	
	    	
	    	// On extrait la grille de sudoku de l'image
	    	try {
	    		
	    		// Parcour les lignes
		    	for(int i = 0; i<9; i++){

		    		// Parcour les colonnes
		    		for(int j = 0; j<9; j++){
		    			// Sort du traitement (arret du thread en cas de demande)
		    			if(threadStopRequest == true) return;
		    			
		    			// Extrait la cellule
		    	    	imgCell = img.submat((int)y, (int)(y + sizeCell), (int)x, (int)(x + sizeCell));
		    	    	
		    	    	// Recupere le nombre renvoy� par la fonction
	    	    		number = lecteur.lireTextFromImage(imgCell);
		    			
		    			System.out.println("[" + i + "][" + j +"] = " + number);
		    			
		    			/*
		    			 * La lecture �choue dans le cas ou la case est vide
		    			 * ou bien la fonction de lecture n'a pas �t� develop�e
		    			 * ou bien un autre probl�me est survenu (photo mal cadr�e par exemple)
		    			 */
		    			if(number.matches("echec lecture") == false ){
		    				sudokuMatrice[i][j] = Integer.parseInt(number);												
		    			}
		    			else{
		    				sudokuMatrice[i][j] = 0;
		    			}
		    			
		    			// Incr�mente les x
		    			x += sizeCell;
		    		}
		    		
		    		// Incr�mentate les y
		    		y += sizeCell;
		    		x = w/2 - sc2 - 4*sizeCell;
		    	}
	    	}
	    	catch(Exception e){
	    		System.out.println("Exception e occured");
	    		e.printStackTrace(System.out);
	    		computingImage = false; 
	    		t = new Thread(r); // Instancie la classe Thread et lance dans un thread la fonction ResolveSudoku
	    		return;
	    	}
	    
	    	/*
	    	 * R�solution du sudoku � l'aide de l'algo
	    	 */
	    	Solver solver = new Solver(sudokuMatrice);
	
	    	// Verifie que la grille du sudoku est valide
	    	if(Solver.isValid(solver.grille)){
		    	try {
		    		// R�soud la grille de sudoku
					solver.solve(0);
				} catch (Exception e) {
					e.printStackTrace( );
				}
	    	}
	    	// Si la grille n'est pas valide
		    else {
		    	// Message utilisateur
	    		System.out.println("La lecture du sodoku a �chou�e");
	    		
	    		// Vibration t�l�phone
	    		Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    		vibs.vibrate(500); 
	    		
	    		// Affiche qu'il y a eu un probleme
	    		try {
	    			toast.show();
	    		}
	    		catch(Exception e){
	    			e.printStackTrace(System.out);
	    			// Sort de la fonction
	    			return;
	    		}
	    		
	    		//computingImage = false;
	    	    //t = new Thread(r);
	    	    //return;
	    		
	    		
	    		/*
	    		 * PR�SENTATION
	    		 */
	    	    // Initialise une fausse grille pour la pr�sentation
	    		int fakeGrille[][] = {
	        			{0,4,0,0,0,2,0,1,9},
	        			{0,0,0,3,5,1,0,8,6},
	        			{3,1,0,0,9,4,7,0,0},
	        			{0,9,4,0,0,0,0,0,7},
	        			{0,0,0,0,0,0,0,0,0},
	        			{2,0,0,0,0,0,8,9,0},
	        			{0,0,9,5,2,0,0,4,1},
	        			{4,2,0,1,6,9,0,0,0},
	        			{1,6,0,8,0,0,0,7,0}
	        	};
	    		// Initialise de nouveau l'objet avec la grille rempli manuellement 
	    	    solver = new Solver(fakeGrille);
	    	    // R�soud la grille
				solver.solve(0);
		    }
			
	    	
	    	/*
	    	 * Affiche le sudoku r�solu
	    	 */
	    	x = w/2 - sc2 - 4*sizeCell;
	    	y = 0;
	    	// Recupere la grille du sudoku r�solu
	    	int[][] sudokuResolu = solver.grille;
	    	
	    	// Parcour la grille (ligne)
	    	for(int i = 0; i<9; i++){
	    		// Parcour la grille (colonne)
	    		for(int j = 0; j<9; j++){
	    			// Si le chiffre est diff�rent de 0 alors
	    			if(sudokuResolu[i][j] != 0){
	    				// Dessine le chiffre � l'�cran
		    			listADessiner.add(new PointValue(new Point( x, y ), sudokuResolu[i][j]));
	    			}
	    			
	    			// Incr�mente les x
	    			x += sizeCell;
	    		}
	    		
	    		// Incr�mente les y
	    		y += sizeCell;
	    		x = w/2 - sc2 - 4*sizeCell;
	    	}
	    	
	    	// Indique que l'image n'a plus a etre calcul�
	    	computingImage = false;
    	}
    	
    	// Cr�e un objet thread
    	t = new Thread(r);
    }
    
}
