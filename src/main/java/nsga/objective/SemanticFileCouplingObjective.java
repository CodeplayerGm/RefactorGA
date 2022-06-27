package nsga.objective;

import nsga.Service;
import nsga.datastructure.Chromosome;

import java.util.HashMap;
import java.util.List;

import static nsga.objective.SemanticFileObjective.getAllSrvSemanticCoupling;

/**
 * 语义耦合度
 * 归一化，统计的是分解方案服务之间代码文件的语义相似度的平均值，越大越好
 * File Search 粒度
 */
public class SemanticFileCouplingObjective extends AbstractObjectiveFunction {

    public SemanticFileCouplingObjective() {
        this.objectiveFunctionTitle = "Semantic File Coupling Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        // 得到的是每个srv - 在overloadServiceFileList文件中的idList
        HashMap<Integer, List<Integer>> srvFilesMap = Service.splitChromosomeToServiceFileIdMap(chromosome);
        return getAllSrvSemanticCoupling(srvFilesMap);
    }
}
