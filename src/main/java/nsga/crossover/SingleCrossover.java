package nsga.crossover;

import nsga.datastructure.Chromosome;
import nsga.datastructure.FunctionalAtom;
import nsga.datastructure.GroupItemAllele;
import nsga.datastructure.Population;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SingleCrossover extends AbstractCrossover {

    /**
     * 每个个体选择move交叉的概率
     */
    private double moveCrossover = 0.6;

    /**
     * 已产生的基因型记录
     */
    private final HashSet<Chromosome> historyRecord;

    public SingleCrossover(CrossoverParticipantCreator crossoverParticipantCreator, float crossoverProbability) {
        super(crossoverParticipantCreator);
        this.crossoverProbability = crossoverProbability;
        historyRecord = new HashSet<>();
    }

    public SingleCrossover(CrossoverParticipantCreator crossoverParticipantCreator, float crossoverProbability,
                           double moveCrossover) {
        super(crossoverParticipantCreator);
        this.crossoverProbability = crossoverProbability;
        this.moveCrossover = moveCrossover;
        historyRecord = new HashSet<>();
    }

    public HashSet<Chromosome> getHistoryRecord() {
        return historyRecord;
    }

    /**
     * 选择用于交叉的个体
     * @param num
     * @param population
     * @return
     */
    private List<Integer> selectCrossoverIndividuals(int num, Population population) {
        Set<Integer> selected = new HashSet<>(num);
        int sc = 0;
        while (sc < num) {
            int id = ThreadLocalRandom.current().nextInt(0, population.getPopulace().size());
            if (!selected.contains(id)) {
                selected.add(id);
                sc ++;
            }
        }
        return new ArrayList<>(selected);
    }

    /**
     * 单亲交叉操作
     * @param parent
     * @return
     */
    private Chromosome singleParentCrossover(Chromosome parent) {
        if (parent.getGeneticCode().size() == 1) {
            // 个体只有一个分组项，只能执行pull up交叉
            return pullUpCrossover(parent);
        }
        // 否则需要先确定交叉策略
        return ThreadLocalRandom.current().nextDouble(0, 1.0) <= moveCrossover ?
                moveCrossover(parent) : pullUpCrossover(parent);
    }

    /**
     * pull up交叉
     * @param parent
     * @return
     */
    private Chromosome pullUpCrossover(Chromosome parent) {
        int selectGroupItemId = ThreadLocalRandom.current().nextInt(0, parent.getGeneticCode().size());
        GroupItemAllele selectGroupItem = (GroupItemAllele) parent.getGeneticCode().get(selectGroupItemId);
        int selectFAId = ThreadLocalRandom.current().nextInt(0, selectGroupItem.getGene().size());

        // 拷贝新个体和功能原子
        Chromosome child = new Chromosome(parent);
        GroupItemAllele targetGi = (GroupItemAllele) (child.getGeneticCode().get(selectGroupItemId));
        FunctionalAtom childFA = targetGi.getGene().get(selectFAId);
        // 从原分组项移除
        targetGi.getGene().remove(childFA);
        if (targetGi.getGene().size() == 0) {
            child.getGeneticCode().remove(selectGroupItemId);
        }
        // 添加一个新的分组项
        GroupItemAllele newGroupItem = new GroupItemAllele(new ArrayList<FunctionalAtom>() {{ add(childFA); }});
        child.getGeneticCode().add(newGroupItem);
        return child;
    }

    /**
     * move 交叉
     * @param parent
     * @return
     */
    private Chromosome moveCrossover(Chromosome parent) {
        // 选择要移动的分组项、功能原子
        int srcGroupItemId = ThreadLocalRandom.current().nextInt(0, parent.getGeneticCode().size());
        GroupItemAllele srcGroupItem = (GroupItemAllele) parent.getGeneticCode().get(srcGroupItemId);
        int selectFAId = ThreadLocalRandom.current().nextInt(0, srcGroupItem.getGene().size());

        // 选择目标分组项
        int destGroupItemId;
        do {
            destGroupItemId = ThreadLocalRandom.current().nextInt(0, parent.getGeneticCode().size());
        } while (destGroupItemId == srcGroupItemId);

        // 拷贝新个体、功能原子
        Chromosome child = new Chromosome(parent);
        GroupItemAllele sourceGi = (GroupItemAllele) (child.getGeneticCode().get(srcGroupItemId));
        GroupItemAllele destGi = (GroupItemAllele) (child.getGeneticCode().get(destGroupItemId));
        FunctionalAtom childFA = sourceGi.getGene().get(selectFAId);
        // 移动
        sourceGi.getGene().remove(selectFAId);
        destGi.getGene().add(childFA);
        if (sourceGi.getGene().size() == 0) {
            child.getGeneticCode().remove(srcGroupItemId);
        }
        return child;
    }

    /**
     * 单亲交叉主逻辑：已完成重复基因型的过滤
     * @param population
     * @return
     */
    @Override
    public List<Chromosome> perform(Population population) {
        // 本轮交叉的个体数量，至少1个
        int crossoverNum = population.getPopulace().size() * crossoverProbability > 1 ?
                (int)(population.getPopulace().size() * crossoverProbability) : 1;
        // 本轮交叉的个体
        List<Integer> crossoverIndividuals = selectCrossoverIndividuals(crossoverNum, population);

        // 执行交叉操作
        // 交叉产生的新个体集合
        List<Chromosome> children = new ArrayList<>(crossoverNum);
        for (int id : crossoverIndividuals) {
            Chromosome curIndividual = population.getPopulace().get(id);
            // 基于概率，选择交叉策略，完成交叉返回一个新个体
            Chromosome child = singleParentCrossover(curIndividual);
            if (!historyRecord.contains(child)) {
                historyRecord.add(child);
//                System.out.println("add child " + historyRecord.size());
                children.add(child);
            }
        }
        return children;
    }
}
