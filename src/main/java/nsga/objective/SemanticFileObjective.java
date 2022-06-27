package nsga.objective;

import nsga.datastructure.Chromosome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nsga.PreProcessLoadData.overloadServiceFileSimMatrix;
import static nsga.Service.splitChromosomeToServiceFileIdMap;

public class SemanticFileObjective extends AbstractObjectiveFunction {

    public SemanticFileObjective() {
        this.objectiveFunctionTitle = "Semantic File Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        // 得到的是每个srv - 在overloadServiceFileList文件中的idList
        HashMap<Integer, List<Integer>> srvFilesMap = splitChromosomeToServiceFileIdMap(chromosome);
        double semanticCohesion = getAllSrvSemanticCohesion(srvFilesMap);
        double semanticCoupling = getAllSrvSemanticCoupling(srvFilesMap);
        return semanticCohesion - semanticCoupling;
    }

    public static double getAllSrvSemanticCohesion(HashMap<Integer, List<Integer>> srvFilesMap) {
        double totalCosineSim = 0;
        for (Map.Entry<Integer, List<Integer>> entry : srvFilesMap.entrySet()) {
            totalCosineSim += calculateSingleSrvSemanticCohesion(entry.getValue());
        }
        return totalCosineSim / srvFilesMap.size();
    }

    /**
     * 计算当前服务内两两代码文件之间的余弦相似度的平均值
     * @param srvFiles
     * @return
     */
    public static double calculateSingleSrvSemanticCohesion(List<Integer> srvFiles) {
        int fileNum = srvFiles.size();
        // 当前服务只有一个文件的话，返回比较低的相似度
        if (fileNum == 1) {
            return 0;
        }
        double totalCosineSim = 0;
        for (int i = 0; i < fileNum; i++) {
            for (int j = 0; j < fileNum; j++) {
                Integer f1 = srvFiles.get(i);
                Integer f2 = srvFiles.get(j);
                if (i == j) {
                    totalCosineSim += 1;
                } else if (i > j) {
                    totalCosineSim += overloadServiceFileSimMatrix[f2][f1];
                } else {
                    totalCosineSim += overloadServiceFileSimMatrix[f1][f2];
                }
            }
        }

        return totalCosineSim / fileNum / fileNum;
    }

    public static double getAllSrvSemanticCoupling(HashMap<Integer, List<Integer>> srvFilesMap) {
        double totalSim = 0;
        List<Integer> srvList = new ArrayList<>(srvFilesMap.keySet());
        int srvNum = srvList.size();
        if (srvNum == 1) {
            return 0;
        }

        List<List<Integer>> srvFilesList = new ArrayList<>(srvFilesMap.values());
        for (int i = 0; i < srvFilesList.size(); i++) {
            List<Integer> files1 = srvFilesList.get(i);
            for (int j = i + 1; j < srvFilesList.size(); j++) {
                List<Integer> files2 = srvFilesList.get(j);
                totalSim += calculatePairSrvSemanticCoupling(files1, files2);
            }
        }
        return totalSim * 2 / (srvNum - 1) / srvNum;

    }

    public static double calculatePairSrvSemanticCoupling(List<Integer> files1, List<Integer> files2) {
        if (files1.size() <= 1 && files2.size() <= 1) {
            return 0;
        }
        double totalSim = 0;
        for (int i : files1) {
            for (int j : files2) {
                totalSim += overloadServiceFileSimMatrix[i][j];
            }
        }
        return totalSim / files1.size() / files2.size() / 2;
    }
}
