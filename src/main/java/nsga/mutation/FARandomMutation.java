package nsga.mutation;

import nsga.datastructure.Chromosome;

import static nsga.Service.randomGenerateFAChromosome;

public class FARandomMutation extends AbstractMutation {

    public FARandomMutation(float mutationProbability) {
        super(mutationProbability);
    }

    @Override
    public Chromosome perform(Chromosome chromosome) {
        return randomGenerateFAChromosome();
    }
}
