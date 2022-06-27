package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import nsga.Service;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static nsga.PreProcessLoadData.readFile;
import static nsga.objective.SMQFAObjective.*;

/**
 * 结构性内聚目标：SMQ的内聚性部分
 * 归一化，越大越好
 * FA Search 粒度
 */
public class StructuralFACohesionObjective extends AbstractObjectiveFunction {

    public StructuralFACohesionObjective() {
        this.objectiveFunctionTitle = "Structural FA Cohesion Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFAMap = Service.splitModularAlleleToServiceFAMap(chromosome);
        return getAllServiceCohesion(srvFAMap);
    }

    // 计算提升百分比
    public static void main(String[] args) throws IOException {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faPrograms.json");
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faRandomPrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);
        double originCohesion = getAllServiceCohesion(getTargetProgramSrvFAMap(programs.getJSONObject(0)));
        System.out.println("origin cohesion: " + originCohesion);

        getProgramCohesionImproved(programs, originCohesion);
    }
}
