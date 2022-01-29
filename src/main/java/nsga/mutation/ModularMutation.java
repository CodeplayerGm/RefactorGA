package nsga.mutation;

import nsga.PreProcessLoadData;
import nsga.datastructure.Chromosome;
import nsga.datastructure.IntegerAllele;

import java.util.concurrent.ThreadLocalRandom;

public class ModularMutation extends AbstractMutation {

    private final float breakProbability;

    public ModularMutation(float mutationProbability, float breakProbability) {
        super(mutationProbability);
        this.breakProbability = breakProbability;
    }

    @Override
    public Chromosome perform(Chromosome chromosome) {
        Chromosome child = new Chromosome(chromosome);

        // 随机选择变异的Gene
        int geneIndex = ThreadLocalRandom.current().nextInt(0, PreProcessLoadData.faNums);

        // 分解模块
        int maxModuleId = chromosome.getGeneticCode().stream()
                .mapToInt(gc -> ((IntegerAllele) (gc)).getGene())
                .max().getAsInt();
        if (ThreadLocalRandom.current().nextDouble(0, 1) <= breakProbability) {
            maxModuleId = Math.min(maxModuleId + 1, PreProcessLoadData.faNums - 1);
            child.getGeneticCode().set(geneIndex, new IntegerAllele(maxModuleId));
        } else {
            // 在当前模块内分配
            int nextIndex;
            do {
                nextIndex = ThreadLocalRandom.current().nextInt(0, PreProcessLoadData.faNums);
            } while (nextIndex == geneIndex);
            child.getGeneticCode().set(geneIndex,
                    child.getGeneticCode().get(nextIndex));
        }

        return child;
    }
}
