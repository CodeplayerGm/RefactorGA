import nsga.Configuration;
import nsga.NSGA2;
import nsga.crossover.CrossoverParticipantCreatorProvider;
import nsga.crossover.SimulatedBinaryCrossover;
import nsga.mutation.PolynomialMutation;
import nsga.objectivefunction.ObjectiveProvider;
import nsga.plugin.GeneticCodeProducerProvider;
import nsga.termination.NonDominatedMajority;

public class NonDominatedMajorityTest {

	public static void main(String[] args) {

		NonDominatedMajority ndm = new NonDominatedMajority();
		Configuration configuration = new Configuration(ObjectiveProvider.provideZDTObjectives());

		configuration.setGeneticCodeProducer(GeneticCodeProducerProvider.valueEncodedGeneticCodeProducer(0, 1, false));
		configuration.setCrossover(new SimulatedBinaryCrossover(
			CrossoverParticipantCreatorProvider.selectByBinaryTournamentSelection(),
			20
		));
		configuration.setMutation(new PolynomialMutation(0, 1));
		configuration.setTerminatingCriterion(ndm);
		configuration.setGenerations(200);
		configuration.setPopulationSize(100);
//		configuration.beSilent();

		NSGA2 nsga2 = new NSGA2(configuration);

		nsga2.run();
	}
}
