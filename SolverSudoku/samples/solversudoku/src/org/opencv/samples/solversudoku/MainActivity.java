/**
 * Package
 */
package org.opencv.samples.solversudoku;

/**
 * Import
 */
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.solversudoku.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

/**
 * Programme principal
 * Hérite d'une activité
 * Implemente l'interface "CvCameraViewListener2"
 * @author Fred
 */
public class MainActivity extends Activity implements CvCameraViewListener2 {
    
	/**
     * Attribut de la class 
     */
	private static final String    TAG = "OCVSample::Activity";

	// Mode d'affichage de la caméra
	private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    // Mode d'affichage
    private int                    mViewMode; // Mode a afficher
    private Mat                    mRgba; // Matrice RGBA
    private Mat                    mIntermediateMat;
    private Mat                    mGray; // Matrice de Gris

    // Menu
    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;

    // Camera
    private CameraBridgeViewBase   camera;

    /**
     * 
     */
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        /**
         * 
         */
    	public void onManagerConnected(int status) {
            //
    		switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                	// Debug (uniquement en mode developpement)
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Chargement de la librairie native, apres l'initiliasation de OpenCV
                    System.loadLibrary("mixed_sample");

                    // Active la view de la camera
                    camera.enableView();
                } break;
                default:
                {
                	// Methode parente OpenCV
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /**
     * Constructeur de la class
     */
    public MainActivity() {    	
    	// Debug (uniquement en mode developpement)
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Fonction qui est appélé lors de la création de l'Activitée
     */
    public void onCreate(Bundle savedInstanceState) {
    	// Debug (uniquement en mode developpement)
        Log.i(TAG, "called onCreate");
        // Initialisation
        super.onCreate(savedInstanceState);
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Ouverture (affichage) de la camera
        setContentView(R.layout.solversudoku_surface_view); 

        // Cast en type CameraBridgeViewBase les param ihm de l'objet camera
        camera = (CameraBridgeViewBase) findViewById(R.id.solversudoku_activity_surface_view);
        
        // Ajout un listener (ecoute d'un evenement) sur la camera
        camera.setCvCameraViewListener(this);
    }

    /**
     * Fonction qui crée le menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Mode normal");
        mItemPreviewGray = menu.add("Mode gris");
        mItemPreviewCanny = menu.add("Mode noir/blanc");
        mItemPreviewFeatures = menu.add("Recherche des traits");
        return true;
    }

    /**
     * Fonction qui desactive la camera
     */
    public void onPause()
    {
        super.onPause();
        if (camera != null)
            camera.disableView();
    }

    /**
     * Fonction qui est utilisé comme point d'entrée des animations
     */
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    /**
     * Destruction de l'objet
     */
    public void onDestroy() {
        super.onDestroy();
        if (camera != null)
            camera.disableView();
    }

    /**
     * Fonction appelée lors du démarrage de la caméra
     * 
     */
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    /**
     * Fonction qui arrete la vue de la caméra
     * 
     */
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    /**
     * Fonction qui modifie la matrice de couleur de la camera
     * @return Mat : Matrice de couleur
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
        	// Mode Gris
	        case VIEW_MODE_GRAY:
	        	// Passe l'affichage avec en niveaux de gris
	            Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4); // Source, Var Destination, ...
	            break;
	            
	        // Mode Normal
	        case VIEW_MODE_RGBA:
	            // Passe l'affichage en RGBA
	            mRgba = inputFrame.rgba();
	            break;
	            
	        // Mode Noir et Blanc
	        case VIEW_MODE_CANNY:
	            // Passe l'affichage en Noir et Blanc
	            mRgba = inputFrame.rgba();
	            // Passe en mode Canny avec les params
	            Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
	            // Recupere la transformation dans la matrice de destination
	            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
	            break;
	            
	        // Recherche des traits (diff entre le rgba et les niveaux de gris)
	        case VIEW_MODE_FEATURES:
	        	// Passe en mode Canny
	            Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
	            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

	            // Appel la fonction qui dessine les traits
	            
	            
	            /*
	            // Matrice des couleurs
	            mRgba = inputFrame.rgba();
	            // Matrice des gris
	            mGray = inputFrame.gray();
	            // Recherche les niveaux de gris, puis les selectionnent
	            FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
	            */
	            
	            break;
        }

        // Renvoi la matrice d'affichage (modifiée)
        return mRgba;
    }

    /**
     * Fonction appelé lors d'un click sur un menu
     */
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Debug (uniquement en mode developpement)
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        // Mode d'affichage 
        if (item == mItemPreviewRGBA) 			mViewMode = VIEW_MODE_RGBA;
        else if (item == mItemPreviewGray) 		mViewMode = VIEW_MODE_GRAY;
        else if (item == mItemPreviewCanny) 	mViewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewFeatures) 	mViewMode = VIEW_MODE_FEATURES;

        // Renvoi vrai
        return true;
    }

    /**
     * Signature de la fonction implementé (CvCameraViewListener2)
     * @param matAddrGr
     * @param matAddrRgba
     */
    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}
