/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Chromo
{
/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	public int[] chromo;
	public double rawFitness;
	public double sclFitness;
	public double proFitness;

	// fitness used to compare the performance
	public double showcaseFitness;

/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	private static double randnum;

/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/

/// Generate a random schedule, where each schedule
/// has to have each member of exactly 5 monitors
	public Chromo(int[] monitors){
		this.chromo = new int[35];
		shuffleArray(monitors);

		for(int i = 0; i<35; i++){
			chromo[i] = monitors[i];
		}

		this.rawFitness = 0;    //  All individuals did not play yet
		this.sclFitness = -1;   //  Fitness not yet scaled
		this.proFitness = -1;   //  Fitness not yet proportionalized
	}


/*******************************************************************************
*                                MEMBER METHODS                                *
*******************************************************************************/

	//  Get Alpha Represenation of a Gene **************************************

	public String getGeneAlpha(int geneID){
		String chromo = new String();
		for(int i=0; i<35; i++){
			chromo += this.chromo[i];
		}
		int start = geneID * Parameters.geneSize;
		int end = (geneID+1) * Parameters.geneSize;
		String geneAlpha = chromo.substring(start, end);
		return (geneAlpha);
	}

	//  Get Integer Value of a Gene (Positive or Negative, 2's Compliment) ****

	public int getIntGeneValue(int geneID){
		String geneAlpha = "";
		int geneValue;
		char geneSign;
		char geneBit;
		geneValue = 0;
		geneAlpha = getGeneAlpha(geneID);
		for (int i=Parameters.geneSize-1; i>=1; i--){
			geneBit = geneAlpha.charAt(i);
			if (geneBit == '1') geneValue = geneValue + (int) Math.pow(2.0, Parameters.geneSize-i-1);
		}
		geneSign = geneAlpha.charAt(0);
		if (geneSign == '1') geneValue = geneValue - (int)Math.pow(2.0, Parameters.geneSize-1);
		return (geneValue);
	}

	//  Get Integer Value of a Gene (Positive only) ****************************

	public int getPosIntGeneValue(int geneID){
		String geneAlpha = "";
		int geneValue;
		char geneBit;
		geneValue = 0;
		geneAlpha = getGeneAlpha(geneID);
		for (int i=Parameters.geneSize-1; i>=0; i--){
			geneBit = geneAlpha.charAt(i);
			if (geneBit == '1') geneValue = geneValue + (int) Math.pow(2.0, Parameters.geneSize-i-1);
		}
		return (geneValue);
	}

	//  Mutate a Chromosome Based on Mutation Type *****************************

/// Swap two monitor's timeslots
	public void doMutation(int[][] preferences){
		switch (Parameters.mutationType){
		case 1:
			int x, randint;
			for (int j=0; j<(Parameters.geneSize * Parameters.numGenes); j++){
				x = this.chromo[j];
				randnum = Search.r.nextDouble();
				if (randnum < Parameters.mutationRate){
					randint = Search.r.nextInt(Parameters.geneSize * Parameters.numGenes);
					this.chromo[j] = this.chromo[randint];
					this.chromo[randint] = x;
				}
			}
			break;

		default:
			System.out.println("ERROR - No mutation method selected");
		}

	}

/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/


	public static int selectParent(){
		double rWheel = 0;
		int j = 0;
		int k = 0;

		switch (Parameters.selectType){

		case 1:     // Proportional Selection
			randnum = Search.r.nextDouble();
			for (j=0; j<Parameters.popSize; j++){
				rWheel = rWheel + Search.member[j].proFitness;
				if (randnum < rWheel) return(j);
			}
			break;

		case 3:     // Random Selection
			randnum = Search.r.nextDouble();
			j = (int) (randnum * Parameters.popSize);
			return(j);

		case 2:     //  Tournament Selection
			int randPar1 = (int)(Parameters.popSize * Search.r.nextDouble())%Parameters.popSize;
			int randPar2 = (int)(Parameters.popSize * Search.r.nextDouble())%Parameters.popSize;
			randnum = Search.r.nextDouble();
			int pos1 = 0, pos2 = 0;
			if(Search.member[randPar1].proFitness >= Search.member[randPar2].proFitness){
				pos1 = randPar1;
				pos2 = randPar2;
			}
			else{ 
				pos1 = randPar2;
				pos2 = randPar1;
			}

			if(randnum > 0.7)
				return pos2;
			else
				return pos1;

		default:
			System.out.println("ERROR - No selection method selected");
		}
	return(-1);
	}

	public static void mateParents(int pnum1, int pnum2, Chromo parent1, Chromo parent2, Chromo child1, Chromo child2){

		int xoverPoint1;
		int xoverPoint2;
		int temp = 0;

		switch (Parameters.xoverType){

		case 1:     //  Single Point Crossover
			for(int i = 0; i<35; i++){
				child1.chromo[i] = parent1.chromo[i];
				child2.chromo[i] = parent2.chromo[i];
			}
			break;

		case 2:     // two - point stabilizing crossover, makes sure that the individuals are legal for the ply

			//  Select crossover points
			xoverPoint1 = 1 + (int)(Search.r.nextDouble() * (Parameters.numGenes * Parameters.geneSize-1));
			xoverPoint2 = 1 + (int)(Search.r.nextDouble() * (Parameters.numGenes * Parameters.geneSize-1));

			//  Two-point crossover
			for(int i=0; i<(Parameters.numGenes * Parameters.geneSize); i++){
				if(i>=xoverPoint1 && i<xoverPoint2){
					child1.chromo[i] = parent2.chromo[i];
					child2.chromo[i] = parent1.chromo[i];
			}
				else{
					child1.chromo[i] = parent1.chromo[i];
					child2.chromo[i] = parent2.chromo[i];
				}
			}

			// stebilization of underrepresented/overrepresented monitors
			for(int i=xoverPoint1; i<xoverPoint2; i++){
				int[] graph = checkConsistent(child1);
				if(graph[child1.chromo[i]-1]>5){
					for(int j=0; j<graph.length; j++){
						if(graph[j]<5)
							child1.chromo[i]=j+1;
					}
				}
			}

			for(int i=xoverPoint1; i<xoverPoint2; i++){
				int[] graph = checkConsistent(child2);
				if(graph[child2.chromo[i]-1]>5){
					for(int j=0; j<graph.length; j++){
						if(graph[j]<5)
							child2.chromo[i]=j+1;
					}
				}
			}

		break;

		case 3:     //  Uniform Crossover

		default:
			System.out.println("ERROR - Bad crossover method selected");
		}

		//  Set fitness values back to zero
		child1.rawFitness = -1;   //  Fitness not yet evaluated
		child1.sclFitness = -1;   //  Fitness not yet scaled
		child1.proFitness = -1;   //  Fitness not yet proportionalized
		child2.rawFitness = -1;   //  Fitness not yet evaluated
		child2.sclFitness = -1;   //  Fitness not yet scaled
		child2.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Produce a new child from a single parent  ******************************

	public static void mateParents(int pnum, Chromo parent, Chromo child){

		//  Create child chromosome from parental material
		child.chromo = parent.chromo;

		//  Set fitness values back to zero
		child.rawFitness = -1;   //  Fitness not yet evaluated
		child.sclFitness = -1;   //  Fitness not yet scaled
		child.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Copy one chromosome to another  ***************************************

	public static void copyB2A (Chromo targetA, Chromo sourceB){

		targetA.chromo = sourceB.chromo;

		targetA.rawFitness = sourceB.rawFitness;
		targetA.sclFitness = sourceB.sclFitness;
		targetA.proFitness = sourceB.proFitness;
		targetA.showcaseFitness = sourceB.showcaseFitness;
		return;
	}

	// Method to get create a schedule
	public static void shuffleArray(int[] array) {
        int temp = array.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < temp; i++){
            int j = i + random.nextInt(temp - i);
            swap(array, i, j);
        }
    }

    // swap 2 individuals
    public static void swap(int[] array, int i, int j) {
        int helper = array[i];
        array[i] = array[j];
        array[j] = helper;
    }

    // get a histogram of monitor's quantity
    public static int[] checkConsistent(Chromo X){
    	int[] monitors = new int[7];
    	for(int i = 0; i < 35; i++){
    		if(X.chromo[i]!=0)
    			monitors[X.chromo[i]-1]++;
    	}
    	return monitors;
    }

}   // End of Chromo.java ******************************************************
