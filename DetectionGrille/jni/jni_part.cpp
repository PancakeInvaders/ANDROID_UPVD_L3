#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
	JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial3_Sample3Native_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
	JNIEXPORT jint JNICALL Java_org_opencv_samples_tutorial3_Sample3Native_FindSquares(JNIEnv*, jobject, jlong addrRgba, jint draw);
	double angle(Point pt1, Point pt2, Point pt0);

	JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial3_Sample3Native_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba) {
		Mat& mGr = *(Mat*)addrGray;
		Mat& mRgb = *(Mat*)addrRgba;
		vector<KeyPoint> v;

		FastFeatureDetector detector(50);
		detector.detect(mGr, v);
		for( unsigned int i = 0; i < v.size(); i++ ) {
			const KeyPoint& kp = v[i];
			circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
		}
	}


	JNIEXPORT jint JNICALL Java_org_opencv_samples_tutorial3_Sample3Native_FindSquares(JNIEnv*, jobject, jlong addrRgba, jint draw)
		{
			// Matrice image passé en parametre
			Mat& image = *(Mat*)addrRgba;

			int thresh = 50, N = 4;
			// Booleen Si Trouvé
			int found = 0;

			// Matrice
			Mat pyr, timg, gray0(image.size(), CV_8U), gray;

			// Filtre le bruit de l'image
			pyrDown(image, pyr, Size(image.cols/2, image.rows/2));
			pyrUp(pyr, timg, image.size());

			// Vecteur pour les points du carré
			vector<vector<Point>> contours;

			// Trouve des carrés dans tout les plan de couleur de l'image
			for(int c=1; c<3; c++)
			{
				// Tableau d'entier
				int ch[] = {c, 0};
				// Copie certain canaux de tableau d'entrée pour les canaux spécifiés de tableaux de sortie
				mixChannels(&timg, 1, &gray0, 1, ch, 1);

				// Essai plusieur niveau
				for(int l=0; l<N; l++)
				{
					// Utilise le mode Canny plutot du niveau zero
					// Canny aide a attraper les carrés avec un angle gradient
					if(l == 0)
					{
						// Application de Canny. Prendre le niveau supérieur du curseur
						// Et fixe le niveau 0 (ce qui oblige les bords fusionnés
						Canny(gray0, gray, 0, thresh, 5);

						// Dilate : Permet de supprimer les trous possibles entre les segments de bord
						dilate(gray, gray, Mat(), Point(-1,-1));
					}
					else
					{
						// Applique le niveau si l est différent de 0
						gray = gray0 >= (l+1)*255/N;
					}

					// Trouve les contours and les stoque dans une liste
					findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

					// Déclare un vecteur
					vector<Point> approx;

					// Test tout les contours
					for(size_t i = 0; i < contours.size(); i++)
					{
						// Contour aproximatif avec une précision proportionnelle au périmetre du contour
						approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

						// Les contours carrés doivent avoir 4 sommets après rapprochement des zones relativement grande
						// afin de filtrer les contours bryuants et convexe.
						// Remarque : La valeur absolue d'une zone est utilisée parce que la zone peut être positif ou négatif
						// Conformément à l'orientation du contour
						if(approx.size() == 4 && fabs(contourArea(Mat(approx))) > 1000 && isContourConvex(Mat(approx)))
						{
							double maxCosine = 0;

							for(int j = 2; j < 5; j++)
							{
								// Trouve le maximum de cosinus d'angle entre 2 bords
								double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1])); // fabs : valeur absolue
								maxCosine = MAX(maxCosine, cosine); // Garde le plus grand cosinus entre les 2 valeurs
							}

							// Si le cosinus de tout les angles sont petit, cad que les angles sont d'environ de 90 degre
							// Alors ont dessine le quadrillage du rectangle
							if(maxCosine < 0.3){
								// Dessine avec un point de centre du rectangle
								int x = (int) ((approx[0].x + approx[1].x + approx[2].x + approx[3].x)/4.0);
								int y = (int) ((approx[0].y + approx[1].y + approx[2].y + approx[3].y)/4.0);

								// Si draw equivaut a 1
								if((int)draw){
									// Dessine les traits du rectangle
									line(image, approx[0], approx[1], Scalar(0,50,255,255), 2, 4, 0);
									line(image, approx[1], approx[2], Scalar(0,50,255,255), 2, 4, 0);
									line(image, approx[2], approx[3], Scalar(0,50,255,255), 2, 4, 0);
									line(image, approx[3], approx[0], Scalar(0,50,255,255), 2, 4, 0);
									// Centre de l'image
									circle(image, Point(x,y), 1, Scalar(0,255,0,255));
									circle(image, Point(x,y+20), 1, Scalar(0,255,255,0));
									circle(image, Point(x,y-20), 1, Scalar(0,0,255,255));
								}

								// Return 1 (trouvé)
								found = 1;
								jint result = (jint) found;
								return result;
							}
						}
					}
				}
			}

			// Return 0 (non trouvé)
			jint result = (jint) found;
			return result;
		}


	double angle(Point pt1, Point pt2, Point pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;

		return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}
}
