package nsga.objectivefunction;

import nsga.datastructure.AbstractAllele;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;

import java.util.List;
import java.util.stream.Collectors;

import static nsga.PreProcessLoadData.allCallMatrix;
import static nsga.PreProcessLoadData.allFileList;

public class CohesionObjective extends AbstractObjectiveFunction {

    public CohesionObjective() {
        this.objectiveFunctionTitle = "Cohesion Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        double totalCohesion = 0;
        int serviceNum = chromosome.getGeneticCode().size();

        // 当前服务的内聚计算 - 几个groupItem就是几个新服务
        int index = 0;
        for (AbstractAllele allele : chromosome.getGeneticCode()) {
            GroupItemAllele gi = (GroupItemAllele) allele;
            List<String> serviceFiles = gi.getAllFiles();
            int faNums = gi.getGene().size();
            double cohesion = getSingleServiceCohesion(serviceFiles, faNums);
            totalCohesion += cohesion;
        }

        return totalCohesion / serviceNum;
    }

    public static double getSingleServiceCohesion(List<String> files, int faNums) {
        List<Integer> fileIndexList = files.stream()
                .map(f -> allFileList.indexOf(f))
                .collect(Collectors.toList());
        // 从call graph中找到edge数量
        int edgeNums = 0;
        for (int i = 0; i < fileIndexList.size(); i++) {
            for (int j = 0; j < fileIndexList.size(); j++) {
                if (j != i) {
                    if (allCallMatrix[i][j] > 0) {
                        edgeNums += allCallMatrix[i][j];
                    }
                }
            }
        }
        return edgeNums * 1.0 / files.size();
    }
}
