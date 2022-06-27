package nsga.mutation;

import nsga.datastructure.Chromosome;

import static nsga.Service.randomGenerateFAChromosome;
import static nsga.Service.randomGenerateFileChromosome;

public class FileRandomMutation extends AbstractMutation {

    public FileRandomMutation(float mutationProbability) {
        super(mutationProbability);
    }

    @Override
    public Chromosome perform(Chromosome chromosome) {
        return randomGenerateFileChromosome();
    }
}
