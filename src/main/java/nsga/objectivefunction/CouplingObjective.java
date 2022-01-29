package nsga.objectivefunction;

import nsga.datastructure.AbstractAllele;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nsga.PreProcessLoadData.allCallMatrix;
import static nsga.PreProcessLoadData.allFileList;

public class CouplingObjective extends AbstractObjectiveFunction {

    public CouplingObjective() {
        this.objectiveFunctionTitle = "Cohesion Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        List<String> allServices = new ArrayList<>();
        Map<String, List<String>> curServiceMap = new HashMap<>();
        Map<String, Integer> curFAMap = new HashMap<>();
        int index = 0;
        for (AbstractAllele allele : chromosome.getGeneticCode()) {
            GroupItemAllele gi = (GroupItemAllele) allele;
            String ns = "newService" + index++;
            allServices.add(ns);
            curServiceMap.put(ns, gi.getAllFiles());
            curFAMap.put(ns, gi.getGene().size());
        }
        int serviceNum = chromosome.getGeneticCode().size();

        int[][] serviceEdgeMatrix = new int[serviceNum][serviceNum];
        int len = allFileList.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (allCallMatrix[i][j] > 0) {
                    // 找到serviceIndex，填入矩阵
                    String srcService = getFileService(curServiceMap, allFileList.get(i));
                    String destService = getFileService(curServiceMap, allFileList.get(j));
                    int srcIndex = allServices.indexOf(srcService);
                    int destIndex = allServices.indexOf(destService);
                    if (srcIndex >= 0 && destIndex >= 0 && srcIndex != destIndex) {
                        serviceEdgeMatrix[srcIndex][destIndex] += allCallMatrix[i][j];
                    }
                }
            }
        }

        // 计算两两之间的coup
        double result = 0;
        for (int i = 0; i < serviceNum; i++) {
            for (int j = i + 1; j < serviceNum; j++) {
                String s1 = allServices.get(i), s2 = allServices.get(j);
                int edges = serviceEdgeMatrix[i][j] + serviceEdgeMatrix[j][i];
                int faN = curFAMap.get(s1) * curFAMap.get(s2);
                double curCoup = edges * 1.0 / faN;
                result += curCoup;
            }
        }

        return -result;
    }

    public static String getFileService(Map<String, List<String>> curServiceMap, String file) {
        for(Map.Entry<String, List<String>> entry : curServiceMap.entrySet()) {
            if (entry.getValue().contains(file)) {
                return entry.getKey();
            }
        }
        return "file doesn't belong to any exist service";
    }

}
