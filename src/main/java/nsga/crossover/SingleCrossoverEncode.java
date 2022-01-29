package nsga.crossover;

import com.debacharya.nsgaii.datastructure.*;
import nsga.PreProcessLoadData;
import nsga.datastructure.Chromosome;
import nsga.datastructure.Population;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SingleCrossoverEncode extends AbstractCrossover {



    public SingleCrossoverEncode(CrossoverParticipantCreator crossoverParticipantCreator, float crossoverProbability) {
        super(crossoverParticipantCreator);
        this.crossoverProbability = crossoverProbability;
    }

    private void listCopy(Chromosome src, Chromosome dest, int ss, int ds, int len) {
        for (int i = 0; i < len; i++) {
            dest.getGeneticCode().set(ds + i,
                    src.getGeneticCode().get(ss + i));
        }
    }

    /**
     * 单点交叉操作
     * @return
     */
    private void singlePointCrossover(List<Chromosome> children, Chromosome p1, Chromosome p2, int pointIndex) {
        int maxLen = p1.getLength();
        Chromosome c1 = new Chromosome(p2);
        Chromosome c2 = new Chromosome(p1);

        // 拷贝前半部分
//        listCopy(p1, c1, 0, pointIndex);
//        listCopy(p2, c2, 0, pointIndex);

        // c1头部换成p2尾部
        listCopy(p2, c1, maxLen - 1 - pointIndex, 0, pointIndex + 1);
        // c2尾部换成p1头部
        listCopy(p1, c2, 0, maxLen - 1 - pointIndex, pointIndex + 1);

        if (!PreProcessLoadData.historyRecord.contains(c1)) {
            PreProcessLoadData.historyRecord.add(c1);
//                System.out.println("add child " + historyRecord.size());
            children.add(c1);
        }
        if (!PreProcessLoadData.historyRecord.contains(c2)) {
            PreProcessLoadData.historyRecord.add(c2);
//                System.out.println("add child " + historyRecord.size());
            children.add(c2);
        }
    }


    /**
     * 单点交叉
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


        // 选择用于交叉的双亲和交叉点
        int[] parentArr1 = new int[crossNum];
        int[] parentArr2 = new int[crossNum];
        int[] crossPointArr = new int[crossNum];

        for (int i = 0; i < crossNum; i++) {
            parentArr1[i] = ThreadLocalRandom.current().nextInt(0, parentSize);
            do {
                parentArr2[i] = ThreadLocalRandom.current().nextInt(0, parentSize);
            } while (parentArr1[i] == parentArr2[i]);
            // 交叉点的位置要少一个
            crossPointArr[i] = ThreadLocalRandom.current().nextInt(0, PreProcessLoadData.faNums - 1);
        }

        // 执行交叉操作
        // 交叉产生的新个体集合
        List<Chromosome> children = new ArrayList<>();
        for (int i = 0; i < crossNum; i++) {
            singlePointCrossover(children,
                    population.getPopulace().get(parentArr1[i]),
                    population.getPopulace().get(parentArr2[i]),
                    crossPointArr[i]);
        }

        return children;
    }


}
