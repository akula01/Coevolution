/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Search {

/*******************************************************************************
*                           INSTANCE VARIABLES                                 *
*******************************************************************************/

/*******************************************************************************
*                           STATIC VARIABLES                                   *
*******************************************************************************/

	public static FitnessFunction problem;

	public static Chromo[] member;
	public static Chromo[] child;

	public static Chromo bestOfGenChromo;
	public static int bestOfGenR;
	public static int bestOfGenG;
	public static Chromo bestFitness;
	public static Chromo bestOfRunChromo;
	public static int bestOfRunR;
	public static int bestOfRunG;
	public static Chromo bestOverAllChromo;
	public static int bestOverAllR;
	public static int bestOverAllG;

	public static double sumRawFitness;
	public static double sumRawFitness2;	// sum of squares of fitness
	public static double sumShowFitness;
	public static double sumSclFitness;
	public static double sumProFitness;
	public static double defaultBest;
	public static double defaultWorst;

	public static double averageRawFitnessShown;
	public static double stdevRawFitnessShown;
	public static double averageRawFitness;
	public static double stdevRawFitness;
	public static double sumShowFitness2;

	public static int G;
	public static int R;
	public static Random r = new Random();
	private static double randnum;

	private static int memberIndex[];
	private static double memberFitness[];
	private static int TmemberIndex;
	private static double TmemberFitness;

	private static double fitnessStats[][];  // 0=Avg, 1=Best
	private static double showcaseStats[][];

/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/


/*******************************************************************************
*                             MEMBER METHODS                                   *
*******************************************************************************/


/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

	public static void main(String[] args) throws java.io.IOException{

		Calendar dateAndTime = Calendar.getInstance(); 
		Date startTime = dateAndTime.getTime();
		int[] monitors = {1,1,1,1,1,2,2,2,2,2,3,3,3,3,3,4,4,4,4,4,5,5,5,5,5,6,6,6,6,6,7,7,7,7,7};

	//  Read Parameter File
		System.out.println("\nParameter File Name is: " + args[0] + "\n");
		Parameters parmValues = new Parameters(args[0]);
		double[][] stdivsAVG = new double[Parameters.numRuns][Parameters.generations];
		double[][] stdivsBest = new double[Parameters.numRuns][Parameters.generations];
		double[][] stdivsAVGShow = new double[Parameters.numRuns][Parameters.generations];
		double[][] stdivsBestShow = new double[Parameters.numRuns][Parameters.generations];
		bestFitness = new Chromo(monitors);
	//  Write Parameters To Summary Output File
		String summaryFileName = Parameters.expID + "_summary.txt";
		FileWriter summaryOutput = new FileWriter(summaryFileName);
		FileWriter averageBestFit = new FileWriter("avbestfit.txt");
		parmValues.outputParameters(summaryOutput);

	//	Set up Fitness Statistics matrix
		fitnessStats = new double[2][Parameters.generations];
		showcaseStats = new double[2][Parameters.generations];
		for (int i=0; i<Parameters.generations; i++){
			fitnessStats[0][i] = 0;
			fitnessStats[1][i] = 0;
			showcaseStats[0][i] = 0;
			showcaseStats[0][i] = 0;
		}

	//	Problem Specific Setup - For new new fitness function problems, create
	//	the appropriate class file (extending FitnessFunction.java) and add
	//	an else_if block below to instantiate the problem.

/// Co-evolutionary Scheduling
		if(Parameters.problemType.equals("CSC")){
				problem = new CEScheduling(Parameters.dataInputFileName);
		}
		else System.out.println("Invalid Problem Type");

		System.out.println(problem.name);

	//	Initialize RNG, array sizes and other objects
		r.setSeed(Parameters.seed);
		memberIndex = new int[Parameters.popSize];
		memberFitness = new double[Parameters.popSize];
		member = new Chromo[Parameters.popSize];
		child = new Chromo[Parameters.popSize];
		bestOfGenChromo = new Chromo(monitors);
		bestOfRunChromo = new Chromo(monitors);
		bestOverAllChromo = new Chromo(monitors);

		if (Parameters.minORmax.equals("max")){
			defaultBest = 0;
			defaultWorst = 999999999999999999999.0;
		}
		else{
			defaultBest = 999999999999999999999.0;
			defaultWorst = 0;
		}

		bestOverAllChromo.rawFitness = defaultBest;

		//  Start program for multiple runs
		for (R = 1; R <= Parameters.numRuns; R++){

			bestOfRunChromo.rawFitness = defaultBest;
			System.out.println();

			//	Initialize First Generation
			for (int i=0; i<Parameters.popSize; i++){
				member[i] = new Chromo(monitors);
				child[i] = new Chromo(monitors);
			}

			//	Begin Each Run
			for (G=0; G<Parameters.generations; G++){

				sumProFitness = 0;
				sumSclFitness = 0;
				sumRawFitness = 0;
				sumShowFitness = 0;
				sumRawFitness2 = 0;
				sumShowFitness2 = 0;

				bestOfGenChromo.rawFitness = defaultBest;

				//	Test Fitness of Each Member
				for (int i=0; i<Parameters.popSize; i++){

					member[i].rawFitness = 0;
					member[i].sclFitness = 0;
					member[i].proFitness = 0;

					problem.doRawFitness(member[i], member);
					member[i].showcaseFitness = printfitness(member[i], problem.preferences);

					sumRawFitness = sumRawFitness + member[i].rawFitness;
					sumShowFitness += member[i].showcaseFitness;
					sumShowFitness2 = sumRawFitness2 +
						member[i].showcaseFitness * member[i].showcaseFitness;
					sumRawFitness2 = sumRawFitness2 +
						member[i].rawFitness * member[i].rawFitness;
					if(member[i].showcaseFitness >= bestFitness.showcaseFitness){
						bestFitness.showcaseFitness = member[i].showcaseFitness;
						for(int m = 0; m<35; m++){
							bestFitness.chromo[m] = member[i].chromo[m];
						}
					}
					if (Parameters.minORmax.equals("max")){
						if (member[i].rawFitness > bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness > bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness > bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
					else {
						if (member[i].rawFitness < bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness < bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness < bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
				}

				// Accumulate fitness statistics
				fitnessStats[0][G] += sumRawFitness / Parameters.popSize;
				fitnessStats[1][G] += bestOfGenChromo.rawFitness;
				showcaseStats[0][G]+= sumShowFitness/Parameters.popSize;
				showcaseStats[1][G]+= bestOfGenChromo.showcaseFitness;


				averageRawFitness = sumRawFitness / Parameters.popSize;
				averageRawFitnessShown = sumShowFitness / Parameters.popSize;
				stdevRawFitnessShown = Math.sqrt(
							Math.abs(sumShowFitness2 - 
							sumShowFitness*sumShowFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							); 
				stdevRawFitness = Math.sqrt(
							Math.abs(sumRawFitness2 - 
							sumRawFitness*sumRawFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Output generation statistics to screen
				System.out.println(R + "\t" + G +  "\t" + (int)bestOfGenChromo.rawFitness + "\t" + averageRawFitness + "\t" + stdevRawFitness);

				// Output generation statistics to summary file
				summaryOutput.write(" R ");
				Hwrite.right(R, 3, summaryOutput);
				summaryOutput.write(" G ");
				Hwrite.right(G, 3, summaryOutput);
				Hwrite.right((int)bestOfGenChromo.rawFitness, 7, summaryOutput);
				Hwrite.right(averageRawFitness, 11, 3, summaryOutput);
				Hwrite.right(stdevRawFitness, 11, 3, summaryOutput);
				summaryOutput.write("\n");

				stdivsBest[R-1][G] = bestOfRunChromo.rawFitness;
				stdivsAVG[R-1][G] = averageRawFitness;

				stdivsBestShow[R-1][G] = bestOfRunChromo.showcaseFitness;
				stdivsAVGShow[R-1][G] = averageRawFitnessShown;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



		// *********************************************************************
		// **************** SCALE FITNESS OF EACH MEMBER AND SUM ***************
		// *********************************************************************

				switch(Parameters.scaleType){

				case 0:     // No change to raw fitness
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = member[i].rawFitness + .000001;
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 1:     // Fitness not scaled.  Only inverted.
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = 1/(member[i].rawFitness + .000001);
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 2:     // Fitness scaled by Rank (Maximizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=Parameters.popSize-1; i>0; i--){
						for (int j=0; j<i; j++){
							if (memberFitness[j] > memberFitness[j+1]){
								TmemberIndex = memberIndex[j];
								TmemberFitness = memberFitness[j];
								memberIndex[j] = memberIndex[j+1];
								memberFitness[j] = memberFitness[j+1];
								memberIndex[j+1] = TmemberIndex;
								memberFitness[j+1] = TmemberFitness;
							}
						}
					}
					//  Copy ordered array to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				case 3:     // Fitness scaled by Rank (minimizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=1; i<Parameters.popSize; i++){
						for (int j=(Parameters.popSize - 1); j>=i; j--){
							if (memberFitness[j-i] < memberFitness[j]){
								TmemberIndex = memberIndex[j-1];
								TmemberFitness = memberFitness[j-1];
								memberIndex[j-1] = memberIndex[j];
								memberFitness[j-1] = memberFitness[j];
								memberIndex[j] = TmemberIndex;
								memberFitness[j] = TmemberFitness;
							}
						}
					}
					//  Copy array order to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				default:
					System.out.println("ERROR - No scaling method selected");
				}

		// *********************************************************************
		// ****** PROPORTIONALIZE SCALED FITNESS FOR EACH MEMBER AND SUM *******
		// *********************************************************************

				for (int i=0; i<Parameters.popSize; i++){
					member[i].proFitness = member[i].sclFitness/sumSclFitness;
					sumProFitness = sumProFitness + member[i].proFitness;
				}

		// *********************************************************************
		// ************ CROSSOVER AND CREATE NEXT GENERATION *******************
		// *********************************************************************

				int parent1 = -1;
				int parent2 = -1;

				//  Assumes always two offspring per mating
				for (int i=0; i<Parameters.popSize; i=i+2){

					//	Select Two Parents
					parent1 = Chromo.selectParent();
					parent2 = parent1;
					while (parent2 == parent1){
						parent2 = Chromo.selectParent();
					}

					//	Crossover Two Parents to Create Two Children
					randnum = r.nextDouble();
					if (randnum < Parameters.xoverRate){
						Chromo.mateParents(parent1, parent2, member[parent1], member[parent2], child[i], child[i+1]);
					}
					else {
						Chromo.mateParents(parent1, member[parent1], child[i]);
						Chromo.mateParents(parent2, member[parent2], child[i+1]);
					}
				} // End Crossover

				//	Mutate Children
				for (int i=0; i<Parameters.popSize; i++){
					child[i].doMutation(problem.preferences);
				}

				//	Swap Children with Last Generation
				for (int i=0; i<Parameters.popSize; i++){
					Chromo.copyB2A(member[i], child[i]);
				}

			} //  Repeat the above loop for each generation

			Hwrite.left(bestOfRunR, 4, summaryOutput);
			Hwrite.right(bestOfRunG, 4, summaryOutput);

			problem.doPrintGenes(bestOfRunChromo, summaryOutput);
			averageBestFit.write(R + "\t" + (int)bestOfRunChromo.rawFitness+"\n");
			System.out.println(R + "\t" + "B" + "\t"+ (int)bestOfRunChromo.rawFitness);
			System.out.println(bestFitness.showcaseFitness + " The best one yet: " + Arrays.toString(bestFitness.chromo));

		} //End of a Run
		Hwrite.left("B", 8, summaryOutput);

		problem.doPrintGenes(bestOverAllChromo, summaryOutput);

		//	Output Fitness Statistics matrix
		summaryOutput.write("\nGen\t\t\tAvgFit\t\t\tBestFit\t\t\tSTD DIV of AV\t\t\t95 conf of best\t\t\tSTD DIV of BEST\n");
		for (int i=0; i<Parameters.generations; i++){
			Hwrite.left(i, 20, summaryOutput);
			Hwrite.left(showcaseStats[0][i]/Parameters.numRuns, 20, 2, summaryOutput);
			Hwrite.left(showcaseStats[1][i]/Parameters.numRuns, 20, 2, summaryOutput);

			double avg = showcaseStats[0][i]/Parameters.numRuns, max = 0;
			for(int j =0; j<Parameters.numRuns; j++){
				stdivsAVGShow[j][i] = (stdivsAVGShow[j][i] - avg) * (stdivsAVGShow[j][i] - avg);
				max += stdivsAVGShow[j][i]; 
			}
			max = max/Parameters.numRuns;
			max = Math.sqrt(max);
			Hwrite.left(max, 20, 2, summaryOutput);

			double confMax = avg + 1.96 * (max/Math.sqrt(Parameters.numRuns));
			double confMin = avg - 1.96 * (max/Math.sqrt(Parameters.numRuns));
			Hwrite.left(confMin, 20, 2, summaryOutput);
			Hwrite.left(confMax, 20, 2, summaryOutput);
			max = 0;

			avg = showcaseStats[1][i]/Parameters.numRuns;
			for(int j =0; j<Parameters.numRuns; j++){
				stdivsBestShow[j][i] = (stdivsBestShow[j][i] - avg) * (stdivsBestShow[j][i] - avg);
				max += stdivsBestShow[j][i]; 
			}
			max = max/Parameters.numRuns;
			max = Math.sqrt(max);
			Hwrite.left(max, 20, 2, summaryOutput);
			max = 0;
			summaryOutput.write("\n");
		}

		summaryOutput.write("\n");
		summaryOutput.close();
		averageBestFit.close();

		System.out.println();
		System.out.println("Start:  " + startTime);
		dateAndTime = Calendar.getInstance(); 
		Date endTime = dateAndTime.getTime();
		System.out.println("End  :  " + endTime);

	} // End of Main Class

// return the below specified fitness
public static double printfitness(Chromo X, int[][] preferences){
       double fitness = 0;
       int index = 0;
       fitness += calculateFitness(preferences, X.chromo);
       return fitness;

}
   

// Fitness calculation method for representation TimeSlots
private static int calculateFitness(int[][] preferences, int[] chromo) {
	int monitor_fitness = 0;
	for(int i=0; i<chromo.length; i++) {
		int monitor_priority = preferences[chromo[i]-1][i];
   		switch(monitor_priority){
   			case 5:
   				monitor_fitness -= 1000;
   				break;
   			case 1:
   				monitor_fitness += 500;
   				break;
   			case 2:
   				monitor_fitness += 200;
   				break;
   			case 3:
   				monitor_fitness += 100;
   				break;
   			case 4:
   				monitor_fitness += 50;
   				break;
   			default:
   				break;
   			}
	}
	return monitor_fitness;
}
}   // End of Search.Java ******************************************************

