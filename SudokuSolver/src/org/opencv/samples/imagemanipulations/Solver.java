package org.opencv.samples.imagemanipulations;

import java.util.BitSet;

/**
 * Class qui permet de retourner une grille de sudoku résolue
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
	 * @param k : (valeur cherchée)
	 * @return boolean : Retourne True si la valeur est déjà présente, Sinon False 
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
	 * @param k : (valeur cherchée)
	 * @return boolean : Retourne True si la valeur est déjà présente, Sinon False
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
	 * @param k : valeur cherchée
	 * @param grille
	 * @param i : ligne
	 * @param j : colonne
	 * @return boolean : Retourne True si la valeur est déjà présente, Sinon False
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
	 * Fonction qui résoud une grille de sudoku, de manière récursive
	 * Utilisation du backtracking
	 * @param int : Position
	 * @return boolean
	 */
	public boolean solve (int position) {
		// Si on a fini de parcourir la grille, retourne vrai
		if (position == this.grille.length*this.grille.length) return true;
		
		// Recupere les coordonnées de la case i et j
		int i = position/this.grille.length; 
		int j = position%this.grille.length;
		
		// Si la case n'est pas vide, on passe à la suivante
		if (this.grille[i][j] != 0) return solve(position+1);

		/*
		 * BACKTRACKING
		 */
		// Enumeration des valeurs possible
		for (int k=1; k <= this.grille.length; k++) {
			// Si la valeur est absente (autorisée)
			if (absentSurLigne(k,i) && absentSurColonne(k,j) && absentSurBloc(k,i,j)) {
				// Enregistrement de k dans la grille
				this.grille[i][j] = k;
					
				// Appel récursif de la fonction, afin de vérifier si par la suite ce choix est correct.
				if (solve(position+1)) return true;
			}
		}
		
		// Tous les chiffres ont été testés, aucun n'est bon, on réinitialise la case
		this.grille[i][j] = 0;
		return false;
	}
	
	/**
	 * Methode qui verifie si la grille est valide
	 * @param board
	 * @return boolean
	 */
	public static boolean isValid(int[][] board)
	{
		// Verifie les lignes et les colonnes
		for (int i = 0; i < board.length; i++)
		{
			BitSet bsRow = new BitSet( 9);
			BitSet bsColumn = new BitSet( 9);
			for (int j = 0; j < board[i].length; j++)
			{
				if (board[i][j] == 0 || board[j][i] == 0)
					continue;
				if (bsRow.get( board[i][j] - 1) || bsColumn.get( board[j][i] - 1))
					return false;
				else
				{
					bsRow.set( board[i][j] - 1);
					bsColumn.set( board[j][i] - 1);
				}
			}
		}
		
		// Verifie la taille de la grile
		for (int rowOffset = 0; rowOffset < 9; rowOffset += 3)
		{
			for (int columnOffset = 0; columnOffset < 9; columnOffset += 3)
			{
				BitSet threeByThree = new BitSet( 9);
				for (int i = rowOffset; i < rowOffset + 3; i++)
				{
					for (int j = columnOffset; j < columnOffset + 3; j++)
					{
						if (board[i][j] == 0)
							continue;
						if (threeByThree.get( board[i][j] - 1))
							return false;
						else
							threeByThree.set( board[i][j] - 1);
					}
				}
			}
		}
		
		// Renvoie Vrai
		return true;
	}
}
