package org.opencv.samples.tutorial3;

public class MainTestSolverSudokuJava {
	
    public static void main(String args[]) {
    	// Grille de sudoku
    	int grille[][] = {
    			{0,4,0,0,0,2,0,1,9},
    			{0,0,0,3,5,1,0,8,6},
    			{3,1,0,0,9,4,7,0,0},
    			{0,9,4,0,8,0,0,0,7},
    			{0,0,0,0,0,0,0,0,0},
    			{2,0,0,0,0,0,8,9,0},
    			{0,0,9,5,2,0,0,4,1},
    			{4,2,0,1,6,9,0,0,0},
    			{1,6,0,8,0,0,0,7,0}
    	};
    	
    	// Crée un objet solver sudoku en initialisant la grille
    	Solver solverSudoku = new Solver(grille); 
    	
    	// Affiche la grille avant
    	System.out.println("Grille avant résolution : ");
    	solverSudoku.affiche();
    	
    	// Resoud le sudoku
    	solverSudoku.estValide(0);
    	
    	// Saute une ligne
    	System.out.println("*************************");
    	
    	// Affiche la grille après
    	System.out.println("Grille après résolution : ");
    	solverSudoku.affiche();
    }
    
}
