package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import nsga.Service;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static nsga.PreProcessLoadData.readFile;
import static nsga.objective.SMQFileObjective.*;

/**
 * 结构性内聚目标：SMQ的内聚性部分
 * 归一化，越大越好
 * File Search 粒度
 */
public class StructuralFileCohesionObjective extends AbstractObjectiveFunction {

    public StructuralFileCohesionObjective() {
        this.objectiveFunctionTitle = "Structural File Cohesion Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFilesMap = Service.splitChromosomeToServiceFileIdMap(chromosome);
        return getAllServiceCohesion(srvFilesMap);
    }

    // 计算提升百分比
    public static void main(String[] args) throws IOException {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\filePrograms.json");
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\fileRandomPrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);
        HashMap<Integer, List<Integer>> originMap = getTargetSrvFilesMap(programs.getJSONObject(0));
        double originCohesion = getAllServiceCohesion(originMap);
        System.out.println("origin cohesion: " + originCohesion);

        getProgramCohesionImproved(programs, originCohesion);
    }
}
