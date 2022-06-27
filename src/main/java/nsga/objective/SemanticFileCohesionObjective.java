package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.Service;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.*;

import static nsga.PreProcessLoadData.*;
import static nsga.objective.SemanticFileObjective.getAllSrvSemanticCohesion;

/**
 * 语义内聚性目标
 * 归一化，统计的是分解方案所有服务的语义内聚值的平均值，越大越好
 * File Search 粒度
 */
public class SemanticFileCohesionObjective extends AbstractObjectiveFunction {

    public SemanticFileCohesionObjective() {
        this.objectiveFunctionTitle = "Semantic File Cohesion Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFilesMap = Service.splitChromosomeToServiceFileIdMap(chromosome);
        return getAllSrvSemanticCohesion(srvFilesMap);
    }

    // 计算提升百分比
    public static void main(String[] args) throws IOException {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\filePrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);

        JSONObject originProgram = programs.getJSONObject(0);
        HashMap<Integer, List<Integer>> originMap = new HashMap<>();
        JSONArray originServices = originProgram.getJSONArray("children");
        for (int j = 0; j < originServices.size(); j++) {
            JSONObject originSrv = originServices.getJSONObject(j);
            JSONArray faArr = originSrv.getJSONArray("children");
            List<Integer> fileIdList = new ArrayList<>();
            for (int k = 0; k < faArr.size(); k++) {
                JSONObject faObj = faArr.getJSONObject(k);
                JSONArray fileArr = faObj.getJSONArray("children");
                for (int i = 0; i < fileArr.size(); i++) {
                    fileIdList.add(overloadServiceFileList.indexOf(fileArr.getJSONObject(i).getString("name")));
                }
            }
            originMap.put(j + 1, fileIdList);
        }
        double originCohesion = getAllSrvSemanticCohesion(originMap);
        System.out.println("origin semantic cohesion: " + originCohesion);

        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvToFileIdListMap = new HashMap<>();
            JSONArray services = program.getJSONArray("children");
            for (int j = 0; j < services.size(); j++) {
                JSONObject service = services.getJSONObject(j);
                JSONArray fas = service.getJSONArray("children");
                srvToFileIdListMap.put(j + 1, new ArrayList<>());
                for (int k = 0; k < fas.size(); k++) {
                    String faName = fas.getJSONObject(k).getString("name");
                    srvToFileIdListMap.get(j + 1).add(Integer.parseInt(faName.substring("File - ".length())));
                }
            }
            double cohesion = getAllSrvSemanticCohesion(srvToFileIdListMap);
            System.out.println("cohesion: " + cohesion);
            System.out.println("提升：" + (cohesion - originCohesion) / originCohesion);
        }
    }
}
