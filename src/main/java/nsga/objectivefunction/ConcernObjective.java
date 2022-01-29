package nsga.objectivefunction;

import nsga.datastructure.AbstractAllele;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static nsga.PreProcessLoadData.*;

public class ConcernObjective extends AbstractObjectiveFunction {

    public ConcernObjective() {
        this.objectiveFunctionTitle = "Concern Overload Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        // 整理出分组项内功能原子对应的全部代码文件
        double f = 1;
        double overloadSum = 0;
        double moduleSum = 0;

        List<Integer> containsConcern = new ArrayList<>();
        for (AbstractAllele allele : chromosome.getGeneticCode()) {
            GroupItemAllele group = (GroupItemAllele) allele;
            // 整理出全部的代码文件
            List<String> fileList = group.getGene().stream()
                    .flatMap(fa -> fa.fileList.stream())
                    .collect(Collectors.toList());
            double concernMetric = 0;
            // 对每个代码文件
            for (String file : fileList) {
                int fileId = globalFileList.indexOf(file);
                // 找出相关的主题
                List<Double> topics = fileTopic.get(fileId);
                List<Integer> curConcerns = new ArrayList<>();
                // 遍历每个topic
                for (int i = 0; i < topics.size(); i++) {
                    // 基于file_threshold和关注点进行过滤
                    if (topics.get(i) >= file_threshold && concerns.contains(i)) {
                        curConcerns.add(i);
                    }
                }
                // 计算当前代码文件的分数
                for (int c : curConcerns) {
                    // 关注点的TC值乘以当前关注点的概率
                    concernMetric += concernTCMap.get(c) * topics.get(c);
                    if (!containsConcern.contains(c)) {
                        containsConcern.add(c);
                    }
                }
            }

            overloadSum += concernMetric;
            moduleSum += Math.abs(concernMetric - overload_threshold);
            if (concernMetric >= overload_threshold) {
                f = -1;
            }
        }

//        if (1 / (f * (overloadSum + moduleNum_threshold * moduleSum)) == 0.05) {
//            System.out.println("concerns: " + containsConcern + " ; " + containsConcern.size());
//            System.out.println(overloadSum + ";" + moduleSum);
//        }
        // 最终适应度
        return 1 / (f * (overloadSum + moduleNum_threshold * moduleSum));
    }
}
