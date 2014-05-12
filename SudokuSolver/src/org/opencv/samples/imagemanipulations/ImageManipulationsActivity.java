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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG                 = "OCVSample::Activity";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_POSTERIZE = 7;

    private MenuItem             mItemVideo;
    private MenuItem             mItemPreviewSudoku;
    private CameraBridgeViewBase mOpenCvCameraView;



    private Mat                  mIntermediateMat;
    private int                  mHistSizeNum = 25;
    private Mat                  mSepiaKernel;

    public static int           viewMode = VIEW_MODE_RGBA;
    
    Mat rgba;
    Size sizeRgba;
    double w;
    double h;
    double sc2;
    double sizeCell;
    double w2;
    
    TextView messageTextView;
    
    LectureIMG lecteur;
    
    ArrayList<PointValue> listADessiner;
    
    private Runnable r = new Runnable() {
		
		@Override
		public void run() {
			
			resolveSudoku();
		}
	};
	
	Thread t = new Thread(r);
    
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

    public ImageManipulationsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	lecteur = new LectureIMG(getAssets());
    	
    	setContentView(R.layout.image_manipulations_surface_view);
    	
    	messageTextView = (TextView)findViewById(R.id.text_id);
    	
    	messageTextView.setVisibility(View.INVISIBLE);
    	
  // TODO Faire en sorte que le TextView soit invisible lorsqu'on lance l'application, puis, passe visible lorsqu'on lance le solve
    	
    	listADessiner = new ArrayList<PointValue>();
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_manipulations_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemVideo  = menu.add("Video");
        mItemPreviewSudoku = menu.add("Sudoku");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemVideo)
            viewMode = VIEW_MODE_RGBA;
        else if (item == mItemPreviewSudoku)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        sizeRgba = rgba.size();



        switch (ImageManipulationsActivity.viewMode) {
        case ImageManipulationsActivity.VIEW_MODE_RGBA:
            break;

        case ImageManipulationsActivity.VIEW_MODE_POSTERIZE:
        	
        	int thickness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
            if(thickness > 5) thickness = 3;
            
            w = sizeRgba.width;
            h = sizeRgba.height;
            
            w2 = w/2;
            
            
            sizeCell = h/9;
            
            sc2 = sizeCell /2;
            
            Point p1;
            Point p2;
            
            // HORIZONTAL
            
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
            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);			// middle top
            p1 = new Point(w2 + sc2, 0);
            p2 = new Point(w2 + sc2, h);
            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);			// middle bot
            
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
            
            
            
            
            // VERTICAL
            
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
            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);			// middle top
            p1 = new Point(w/2 - h/2, h/2 + sc2);
            p2 = new Point(w/2 + h/2, h/2 + sc2);
            Core.line(rgba, p1, p2, new Scalar(250,250,250), thickness);			// middle bot
            
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
            
            
            
            
            break;
        }
        
        // on dessine les chiffres de la grille résolue
        
        double x;
        double y;
        
        for(PointValue pv : listADessiner){
        	
        	
        	x = pv.getPoint().x;
        	y = pv.getPoint().y;
        	
        	
            Core.putText(rgba, "" + pv.getValue(), new Point (x + (sc2/2) - 5, y + sizeCell - (sc2/2)  + 7), Core.FONT_HERSHEY_SIMPLEX, 3, new Scalar(25,25,25), 10);
        
        
        }
        



        return rgba;
    }
    
    
    public void resolveSudokuThread(View view){
    	

    	messageTextView.setVisibility(View.VISIBLE);
    	
    // TODO Faire en sorte que le TextView soit invisible lorsqu'on lance l'application, puis, passe visible lorsqu'on lance le solve


    	if(t.isAlive() == false){
    		t.start();
    	}
    }
    
    
    public void resolveSudoku() {
    	
    	if (ImageManipulationsActivity.viewMode == ImageManipulationsActivity.VIEW_MODE_POSTERIZE ){
	     
			 	   // extraire grille de l'img , puis résoude la grille, puis corriger l'img
			    	
			    	// instantiation de la matrice du sudoku à 0, de cette manière si il y a un 0, ça veux dire que c'est pas instancié
			    	// les chiffres valables étant compris entre 1 et 9
			    	// pour acceder à une case : matriceSudoku.get(x).get(y), en supposant qu'on a mit le telephone dans sa largeur
			    	
			    	
			    	// fin de l'instanciation
			  
			    	
			    	// Je part du principe que j'ai une fonction servant à lire un nombre dans une image 
			    	// cette fonction ayant pour signature static String lireTextFromImage(Mat img);
			    	
			    	System.out.println("TEST");
			    	
			    	//messageTextView.setVisibility(View.VISIBLE);
			    	
			    	
			    	
			    	
			    	Mat imgCell;
			    	String number;
			    	
			    	double x = w/2 - sc2 - 4*sizeCell;
			    	double y = 0;
			    	
			    	System.out.println("x: " + x + "y: " + y);
			    	
			    	// x et y representent le point en haut gauche de la cellule à lire, on les initialise en haut à gauche de la grille de sudoku
			    	
			    	int[][] sudokuMatrice = new int[9][9];	// initialisation de la matrice du sudoku
			    	
			    	
			    	// on extrait la grille de sudoku de l'image
			    	
			    	try {
			    	
				    	for(int i = 0; i<9; i++){
				    		
				    		for(int j = 0; j<9; j++){
				    			
				    	    	imgCell = rgba.submat((int)y, (int)(y + sizeCell), (int)x, (int)(x + sizeCell));
				    	    	
			  		    		System.out.println("[" + i + "][" + j + "] passed");

				    			
				    			number = lecteur.lireTextFromImage(imgCell);
				    			
				    			System.out.println(number);
				    			
				    			if( number.matches("echec lecture") == false ){
				    				
				    																// la lecture échoue dans le cas ou la case est vide
				    																// ou bien la fonction de lecture n'a pas été developée
				    				sudokuMatrice[i][j] = Integer.parseInt(number);	// ou bien un autre problème est survenu 
				    																// (photo mal cadrée par exemple)
				    			}
				    			else{
				    				
				    				sudokuMatrice[i][j] = 0;
				    			}
				    			
				    			x = x + sizeCell;
				    			
				    		}
				    		
				    		y = y + sizeCell;
				    		x = w/2 - sc2 - 4*sizeCell;
				    	}
			    	
			    	}
			    	catch(Exception e){
			    		
			    		System.out.println("Exception e occured");
			    		
			    		e.printStackTrace(System.out);
			    		
			    		return;
			    	}
			    
			    	
			    	boolean fullzero = true;
			    	
					for(int i = 0; i<9; i++){
			    		for(int j = 0; j<9; j++){
			    		if(sudokuMatrice[i][j] != 0)
			    		{
			    			fullzero = false;
			    			
			    		}
			    		}
			    	}
			    	
			    	if(fullzero == true){
			    		
			    		Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			    	    vibs.vibrate(500);   
			    		
			    		System.out.println("Aucun chiffre n'a été détecté");
			    		return;
			    	}
			    	
			    	// on résou maintenant le sudoku 
			    	
			    	
			    	
			    	SolveSudoku solver = new SolveSudoku();
			    	
			    	solver.setModel(sudokuMatrice);
			    	
			    	try
					{
						solver.solve( 0 , 0);
					}
					catch (Exception e)
					{
						e.printStackTrace( );
					}
			    	
			    	if( SolveSudoku.isValid( solver.getModel() ) == false){
			    	
			    		System.out.println("La lecture a échouée");
			    		
			    		Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			    	    vibs.vibrate(500); 
			    	    
			    	    return;
			    		
			    	}
					
			    	
			    	
			    	// on affiche le sudoku résolu
			    	
			    	x = w/2 - sc2 - 4*sizeCell;
			    	y = 0;
			    	    	
			    	int[][] sudokuResolu = solver.getModel();
			    	
			    	
			    	x = w/2 - sc2 - 4*sizeCell;
			    	y = 0;
			    	
			    	
			    			// TEST A SUPRIMER
					    	//int[][] sudokuResolu = {
					    	//		{1,1,1,1,1,1,1,1,1},
					    	//		{2,2,2,2,2,2,2,2,2},
					    	//		{3,3,3,3,3,3,3,3,3},
					    	//		{4,4,4,4,4,4,4,4,4},
					    	//		{5,5,5,5,5,5,5,5,5},
					    	//		{6,6,6,6,6,6,6,6,6},
					    	//		{7,7,7,7,7,7,7,7,7},
					    	//		{8,8,8,8,8,8,8,8,8},
					    	//		{9,9,9,9,9,9,9,9,9}
					    	//};

			    	for(int i = 0; i<9; i++){
			    		
			    		for(int j = 0; j<9; j++){
			    			if( sudokuResolu[i][j] != 0){
			    				listADessiner.add(new PointValue(new Point( x, y ), sudokuResolu[i][j] ));
			    			}
			    			
			    			x = x + sizeCell;
			    		}
			    		
			    		y = y + sizeCell;
			    		x = w/2 - sc2 - 4*sizeCell;
			    	}
			    	
			    	messageTextView.setVisibility(View.INVISIBLE);
			    	
			   // TODO Faire en sorte que le TextView soit invisible lorsqu'on lance l'application, puis, passe visible lorsqu'on lance le solve

			    	
			    	
    	}
    }
    
    
}
