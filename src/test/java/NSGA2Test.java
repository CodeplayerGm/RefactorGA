import nsga.Configuration;
import nsga.NSGA2;
import nsga.datastructure.Population;
import nsga.termination.StabilizationOfObjectives;

public class NSGA2Test {

	public static void main(String[] args) {

		Configuration configuration = new Configuration();

		configuration.beSilent();
		configuration.setGenerations(200);
		configuration.setTerminatingCriterion(new StabilizationOfObjectives(0.03d));

		NSGA2 nsga2 = new NSGA2(configuration);
		Population paretoFront = nsga2.run();
	}
}
