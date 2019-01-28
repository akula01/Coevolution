import java.io.*;
import java.util.*;
import java.text.*;

public class CEScheduling extends FitnessFunction{

/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

/*******************************************************************************
*                            STATIC VARIABLES                                  *
*******************************************************************************/


/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/

/// Get the preference Table
/// Each entry corresponds to the preference of individual monitor
/// where the lesser value indicates the greater preference
/// If monitor is not available, the value is 5
	public CEScheduling(String filename) throws java.io.IOException{
		name = "Co-Evolutionary Scheduling Problem";
		preferences = new int[7][35];
		Scanner sc = new Scanner(new File(filename));
		int temp = 0;

		for(int i = 0; i<7; i++){
			sc.next();
			for(int j = 0; j < 35; j++){
				temp = sc.nextInt();
				if(temp == 0){
					temp = 5;
				}
				preferences[i][j] = temp;
			}
		}
	}

/*******************************************************************************
*                                MEMBER METHODS                                *
*******************************************************************************/

//  COMPUTE A CHROMOSOME'S RAW FITNESS *************************************
// compare each timeslot
// If the preference of the one is higher, one gets the point
// Sum of points are compared, the winner gets a point to its raw fitness
	public void doRawFitness(Chromo X, Chromo[] population){
		int scoreX=0, scoreY=0;
		for(int i =0; i<population.length; i++){
			for(int j = 0; j<X.chromo.length; j++){
				if(preferences[X.chromo[j]-1][j] < preferences[population[i].chromo[j]-1][j]){
					scoreX+=1;
				}
				else if(preferences[X.chromo[j]-1][j] > preferences[population[i].chromo[j]-1][j]){
					scoreY+=1;
				}
			}
			if(scoreX>scoreY){
				X.rawFitness+=1;
		}
		scoreX = 0;
		scoreY = 0;
	}
}

//  PRINT OUT AN INDIVIDUAL GENE TO THE SUMMARY FILE *********************************

	public void doPrintGenes(Chromo X, FileWriter output) throws java.io.IOException{

		for (int i=0; i<Parameters.numGenes; i++){
			Hwrite.right(X.getGeneAlpha(i),11,output);
		}
		output.write("   RawFitness");
		output.write("\n        ");
		for (int i=0; i<Parameters.numGenes; i++){
			Hwrite.right(X.getPosIntGeneValue(i),11,output);
		}
		Hwrite.right((int) X.rawFitness,13,output);
		return;
	}
	
/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

}   // End of Scheduling.java ******************************************************

