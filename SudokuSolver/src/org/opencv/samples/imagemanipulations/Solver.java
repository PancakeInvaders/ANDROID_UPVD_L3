package org.opencv.samples.imagemanipulations;

/**
 * Class qui permet de retourner une grille de sudoku r�solue
 * @author Fred
 */
public class Solver {
	
	/**
	 * Attribut
	 */
	public int grille[][];
	
	/**
	 * Constructeur qui init la grille de sudoku
	 * @param grille
	 */
	Solver(int grille[][]){
		this.grille = grille;
	}
	
	/**
	 * Affiche la grille de sudoku
	 */
	public void affiche(){
		for (int i = 0; i < this.grille.length; i++){
			for (int j = 0; j < this.grille.length; j++){
				System.out.print(this.grille[i][j]);
			}
			System.out.print('\n');
		}
	}
	
	/**
	 * Fonction qui cherche l'existance d'une valeur dans la grille sur les lignes
	 * @param k : (valeur cherch�e)
	 * @return boolean : Retourne True si la valeur est d�j� pr�sente, Sinon False 
	 */
	public boolean absentSurLigne(int k, int i) {
		// Parcour la grille sur les lignes
		for (int j=0; j < this.grille.length; j++) {
			// Si la valeur k existe deja, return faux
			if (this.grille[i][j] == k) return false;
		}
		// Si la valeur k n'existe pas, return vrai
		return true;
	}

	/**
	* Fonction qui cherche l'existance d'une valeur dans la grille sur les colonnes
	 * @param k : (valeur cherch�e)
	 * @return boolean : Retourne True si la valeur est d�j� pr�sente, Sinon False
	 */
	public boolean absentSurColonne (int k, int j){
		// Parcour la grille sur les colonnes
		for (int i=0; i < this.grille.length; i++) {
			// Si la valeur k existe deja, return faux
			if (this.grille[i][j] == k) return false;
		}
		// Si la valeur k n'existe pas, return vrai
		return true;
	}
	
	/**
	 * Fonction qui cherche l'existance d'une valeur dans la grille sur un block
	 * @param k : valeur cherch�e
	 * @param grille
	 * @param i : ligne
	 * @param j : colonne
	 * @return boolean : Retourne True si la valeur est d�j� pr�sente, Sinon False
	 */
	public boolean absentSurBloc (int k, int i, int j) {
		int _i = i-(i%3), _j = j-(j%3);  // ou encore : _i = 3*(i/3), _j = 3*(j/3);
			// Parcour les lignes
			for (i=_i; i < _i+3; i++) {
				// Parcour les colonnes
				for (j=_j; j < _j+3; j++) {
					// Si la valeur k existe deja, return faux
					if (this.grille[i][j] == k) return false;
				}
			}
			// Si la valeur k n'existe pas, return vrai
			return true;
	}
	
	/**
	 * Fonction qui r�soud une grille de sudoku, de mani�re r�cursive
	 * Utilisation du backtracking
	 * @return boolean
	 */
	public boolean estValide (int position) {
		// Si on a fini de parcourir la grille, retourne vrai
		if (position == this.grille.length*this.grille.length) return true;
		
		// Recupere les coordonn�es de la case i et j
		int i = position/this.grille.length; 
		int j = position%this.grille.length;
		
		// Si la case n'est pas vide, on passe � la suivante
		if (this.grille[i][j] != 0) return estValide(position+1);

		/*
		 * BACKTRACKING
		 */
		// Enumeration des valeurs possible
		for (int k=1; k <= this.grille.length; k++) {
			// Si la valeur est absente (autoris�e)
			if (absentSurLigne(k,i) && absentSurColonne(k,j) && absentSurBloc(k,i,j)) {
				// ENregistrement de k dans la grille
				this.grille[i][j] = k;
					
				// Appel r�cursif de la fonction, afin de v�rifier si par la suite ce choix est correct.
				if (estValide(position+1)) return true;
			}
		}
		
		// Tous les chiffres ont �t� test�s, aucun n'est bon, on r�initialise la case
		this.grille[i][j] = 0;
		return false;
	}
}
