package nsga.objectivefunction;

import nsga.datastructure.Chromosome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nsga.PreProcessLoadData.*;
import static nsga.Service.getModuleFileList;
import static nsga.Service.splitModularAlleleToServiceFAMap;

public class ConcernNewObjective extends AbstractObjectiveFunction {

    public ConcernNewObjective() {
        this.objectiveFunctionTitle = "Concern Overload Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        double f = 1;
        double overloadSum = 0;
        double moduleSum = 0;

        List<Integer> containsConcern = new ArrayList<>();
        // 整理出每个子模块的FA列表
        HashMap<Integer, List<Integer>> moduleFAMap = splitModularAlleleToServiceFAMap(chromosome);

        for (Map.Entry<Integer, List<Integer>> entry : moduleFAMap.entrySet()) {
            // 子模块的代码文件列表
            List<String> moduleFileList = getModuleFileList(entry.getValue());
            double concernMetric = 0;
            // 对每个代码文件
            for (String file : moduleFileList) {
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

        // 最终适应度
        return 1 / (f * (overloadSum + moduleNum_threshold * moduleSum));
    }
}
