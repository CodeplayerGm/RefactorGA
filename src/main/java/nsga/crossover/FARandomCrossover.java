package nsga.crossover;

import nsga.datastructure.Chromosome;
import nsga.datastructure.Population;

import java.util.ArrayList;
import java.util.List;

import static nsga.PreProcessLoadData.historyRecord;
import static nsga.Service.randomGenerateFAChromosome;
import static nsga.objective.ConcernGranularityObjective.ifChromosomeOverload;

public class FARandomCrossover extends AbstractCrossover {

    public FARandomCrossover(CrossoverParticipantCreator crossoverParticipantCreator, float crossoverProbability) {
        super(crossoverParticipantCreator);
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * 随机交叉
     * @param population
     * @return
     */
    @Override
    public List<Chromosome> perform(Population population) {
        int parentSize = population.getPopulace().size();
        if (parentSize < 2) {
            return new ArrayList<>();
        }

        // 基于交叉概率计算当前代交叉的组数
        int crossNum = (int) (this.crossoverProbability * parentSize);
        crossNum = Math.max(crossNum, 1);

        // 交叉产生的新个体集合
        List<Chromosome> children = new ArrayList<>();
        for (int i = 0; i < crossNum; i++) {
            Chromosome randomChromosome = randomGenerateFAChromosome();
            if (!historyRecord.containsKey(randomChromosome)) {
                historyRecord.put(randomChromosome, ifChromosomeOverload(randomChromosome));
                children.add(randomChromosome);
            }
        }

        return children;
    }


}
