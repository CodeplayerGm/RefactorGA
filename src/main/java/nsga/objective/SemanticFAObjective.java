package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nsga.PreProcessLoadData.readFile;
import static nsga.Service.*;
import static nsga.objective.SMQFAObjective.getTargetProgramSrvFAMap;
import static nsga.objective.SemanticFileObjective.calculatePairSrvSemanticCoupling;
import static nsga.objective.SemanticFileObjective.calculateSingleSrvSemanticCohesion;

public class SemanticFAObjective extends AbstractObjectiveFunction {

    public SemanticFAObjective() {
        this.objectiveFunctionTitle = "Semantic FA Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFAMap = splitModularAlleleToServiceFAMap(chromosome);
        double semanticCohesion = getAllSrvSemanticCohesion(srvFAMap);
        double semanticCoupling = getAllSrvSemanticCoupling(srvFAMap);
        return semanticCohesion - semanticCoupling;
    }

    public static double getAllSrvSemanticCohesion(HashMap<Integer, List<Integer>> srvFAMap) {
        double totalCosineSim = 0;
        for (Map.Entry<Integer, List<Integer>> entry : srvFAMap.entrySet()) {
            List<Integer> serviceFileIdList = getFileIdListFromFAList(entry.getValue());
            totalCosineSim += calculateSingleSrvSemanticCohesion(serviceFileIdList);
        }
        return totalCosineSim / srvFAMap.size();
    }

    public static double getAllSrvSemanticCoupling(HashMap<Integer, List<Integer>> srvFAMap) {
        double totalSim = 0;
        List<Integer> srvList = new ArrayList<>(srvFAMap.keySet());
        int srvNum = srvList.size();
        if (srvNum == 1) {
            return 0;
        }

        List<List<Integer>> srvFAList = new ArrayList<>(srvFAMap.values());
        for (int i = 0; i < srvNum; i++) {
            List<Integer> files1 = getFileIdListFromFAList(srvFAList.get(i));
            for (int j = i + 1; j < srvNum; j++) {
                List<Integer> files2 = getFileIdListFromFAList(srvFAList.get(j));
                totalSim += calculatePairSrvSemanticCoupling(files1, files2);
            }
        }
        return totalSim * 2 / (srvNum - 1) / srvNum;
    }

    public static void getProgramSemanticImproved(JSONArray programs, double originSemantic) {
        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvFAMap = getTargetProgramSrvFAMap(program);
            double cohesion = getAllSrvSemanticCohesion(srvFAMap);
            double coupling = getAllSrvSemanticCoupling(srvFAMap);
            System.out.println("cohesion: " + getFourBitsDoubleString(cohesion) + "; coupling: " + getFourBitsDoubleString(coupling));
            System.out.println("semantic: " + getFourBitsDoubleString(cohesion-coupling));
            System.out.println("提升：" + getFourBitsDoubleString((cohesion - coupling - originSemantic) / originSemantic));
        }
    }

    public static void main(String[] args) throws IOException {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faPrograms-1.json");
        JSONArray programs = JSONArray.parseArray(jsonString);
        double originSemantic = getAllSrvSemanticCohesion(getTargetProgramSrvFAMap(programs.getJSONObject(0)));
        System.out.println("origin semantic: " + getFourBitsDoubleString(originSemantic));

        getProgramSemanticImproved(programs, originSemantic);
    }

}
