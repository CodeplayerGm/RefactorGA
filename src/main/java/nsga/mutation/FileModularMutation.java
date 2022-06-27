package nsga.mutation;

import nsga.datastructure.Chromosome;
import nsga.datastructure.IntegerAllele;

import java.util.concurrent.ThreadLocalRandom;

import static nsga.PreProcessLoadData.overloadServiceFileList;

public class FileModularMutation extends AbstractMutation {

    private final float breakProbability;

    public FileModularMutation(float mutationProbability, float breakProbability) {
        super(mutationProbability);
        this.breakProbability = breakProbability;
    }

    @Override
    public Chromosome perform(Chromosome chromosome) {
        Chromosome child = new Chromosome(chromosome);

        // 随机选择变异的Gene
        int geneIndex = ThreadLocalRandom.current().nextInt(0, overloadServiceFileList.size());

        // 分解模块
        int maxModuleId = chromosome.getGeneticCode().stream()
                .mapToInt(gc -> ((IntegerAllele) (gc)).getGene())
                .max().getAsInt();
        if (ThreadLocalRandom.current().nextDouble(0, 1) <= breakProbability) {
            maxModuleId = Math.min(maxModuleId + 1, overloadServiceFileList.size() - 1);
            child.getGeneticCode().set(geneIndex, new IntegerAllele(maxModuleId));
        } else {
            // 在当前模块内分配
            int nextIndex;
            do {
                nextIndex = ThreadLocalRandom.current().nextInt(0, overloadServiceFileList.size());
            } while (nextIndex == geneIndex);
            child.getGeneticCode().set(geneIndex,
                    child.getGeneticCode().get(nextIndex));
        }

        return child;
    }
}
