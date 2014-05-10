package org.opencv.samples.imagemanipulations;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG                 = "OCVSample::Activity";
    
    // TODO supprimer tout le code commenté qui vient de openCV

    public static final int      VIEW_MODE_RGBA      = 0;
    //public static final int      VIEW_MODE_HIST      = 1;
    //public static final int      VIEW_MODE_CANNY     = 2;
    //public static final int      VIEW_MODE_SEPIA     = 3;
    //public static final int      VIEW_MODE_SOBEL     = 4;
    //public static final int      VIEW_MODE_ZOOM      = 5;
    //public static final int      VIEW_MODE_PIXELIZE  = 6;
    public static final int      VIEW_MODE_POSTERIZE = 7;

    private MenuItem             mItemPreviewRGBA;
    //private MenuItem             mItemPreviewHist;
    //private MenuItem             mItemPreviewCanny;
    //private MenuItem             mItemPreviewSepia;
    //private MenuItem             mItemPreviewSobel;
    //private MenuItem             mItemPreviewZoom;
    //private MenuItem             mItemPreviewPixelize;
    private MenuItem             mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size                 mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private int                  mHistSizeNum = 25;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private float                mBuff[];
    private Mat                  mSepiaKernel;

    public static int           viewMode = VIEW_MODE_RGBA;
    
    Mat rgba;
    Size sizeRgba;
    double w;
    double h;
    double sc2;
    double sizeCell;
    double w2;
    
    ArrayList<PointValue> listADessiner;
    
	private static final int PUZZLE_SIZE = 9;

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
        mItemPreviewRGBA  = menu.add("Preview RGBA");
        //mItemPreviewHist  = menu.add("Histograms");
        //mItemPreviewCanny = menu.add("Canny");
        //mItemPreviewSepia = menu.add("Sepia");
        //mItemPreviewSobel = menu.add("Sobel");
        //mItemPreviewZoom  = menu.add("Zoom");
        //mItemPreviewPixelize  = menu.add("Pixelize");
        mItemPreviewPosterize = menu.add("Posterize");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        //if (item == mItemPreviewHist)
        //    viewMode = VIEW_MODE_HIST;
        //else if (item == mItemPreviewCanny)
        //    viewMode = VIEW_MODE_CANNY;
        //else if (item == mItemPreviewSepia)
        //    viewMode = VIEW_MODE_SEPIA;
        //else if (item == mItemPreviewSobel)
        //    viewMode = VIEW_MODE_SOBEL;
        //else if (item == mItemPreviewZoom)
        //    viewMode = VIEW_MODE_ZOOM;
        //else if (item == mItemPreviewPixelize)
        //    viewMode = VIEW_MODE_PIXELIZE;
        else if (item == mItemPreviewPosterize)
            viewMode = VIEW_MODE_POSTERIZE;
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0  = new Mat();
        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

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

        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        switch (ImageManipulationsActivity.viewMode) {
        case ImageManipulationsActivity.VIEW_MODE_RGBA:
            break;

        /*
         * case ImageManipulationsActivity.VIEW_MODE_HIST:
            Mat hist = new Mat();
            int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
            if(thikness > 5) thikness = 5;
            int offset = (int) ((sizeRgba.width - (5*mHistSizeNum + 4*10)*thikness)/2);
            // RGB
            for(int c=0; c<3; c++) {
                Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for(int h=0; h<mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height-1;
                    mP2.y = mP1.y - 2 - (int)mBuff[h];
                    Core.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
                }
            }
            // Value and Hue
            Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
            // Value
            Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - 2 - (int)mBuff[h];
                Core.line(rgba, mP1, mP2, mWhilte, thikness);
            }
            // Hue
            Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - 2 - (int)mBuff[h];
                Core.line(rgba, mP1, mP2, mColorsHue[h], thikness);
            }
            break;
            */
        /*
        case ImageManipulationsActivity.VIEW_MODE_CANNY:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            rgbaInnerWindow.release();
            break;
		*/
        /*
        case ImageManipulationsActivity.VIEW_MODE_SOBEL:
            Mat gray = inputFrame.gray();
            Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
            Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            grayInnerWindow.release();
            rgbaInnerWindow.release();
            break;
		*/
        /*
        case ImageManipulationsActivity.VIEW_MODE_SEPIA:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
            rgbaInnerWindow.release();
            break;
		*/
        /*
        case ImageManipulationsActivity.VIEW_MODE_ZOOM:
            Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
            Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
            Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
            Size wsize = mZoomWindow.size();
            Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
            zoomCorner.release();
            mZoomWindow.release();
            break;
		*/
            /*
        case ImageManipulationsActivity.VIEW_MODE_PIXELIZE:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
            rgbaInnerWindow.release();
            break;
		*/
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
        
        double x;
        double y;
        
        for(PointValue pv : listADessiner){
        	
        	
        	x = pv.getPoint().x;
        	y = pv.getPoint().y;
        	
        	
            Core.putText(rgba, "" + pv.getValue(), new Point (x + (sc2/2) - 5, y + sizeCell - (sc2/2)  + 7), Core.FONT_HERSHEY_SIMPLEX, 3, new Scalar(25,25,25), 10);
        
        
        }
        
        rgba.submat((int)0, (int)(106), (int)906, (int)(1013));
        


        return rgba;
    }
    
    
    public void resolveSudoku(View view) {
    	
    	if (ImageManipulationsActivity.viewMode == ImageManipulationsActivity.VIEW_MODE_POSTERIZE ){
	     
			 	   // extraire grille de l'img puis corriger l'img
			    	
			    	// instantiation de la matrice du sudoku à 0, de cette manière si il y a un 0, ça veux dire que c'est pas instancié
			    	// les chiffres valables étant compris entre 1 et 9
			    	// pour acceder à une case : matriceSudoku.get(x).get(y), en supposant qu'on a mit le telephone dans sa largeur
			    	
			    	
			    	// fin de l'instanciation
			  
			    	
			    	// Je part du principe que j'ai une fonction servant à lire un nombre dans une image 
			    	// cette fonction ayant pour signature static String lireTextFromImage(Mat img);
			    	
			    	System.out.println("TEST");
			    	
			    	
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

				    			
				    			number = LectureIMG.lireTextFromImage(imgCell);
				    			
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
			    	}
			    	
			    	
			    	//TODO 	verifier si il tous les digits de la grille de sudoku valent 0, dans ce cas, afficher un message
			    	// 		d'erreur, ou faire vibrer l'écran, ou autre
			    	
			    	
			    	
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
			    	
			    		// afficher un message d'erreur, ou autre moyen d'informer l'utilisateur que ça a échoué
			    		System.out.println("La lecture a échouée");
			    		
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
			    	
			    	
    	}
    }
    
    
}
