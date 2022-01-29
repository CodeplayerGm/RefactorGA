package nsga.objectivefunction;

import nsga.datastructure.Chromosome;
import nsga.PreProcessLoadData;
import nsga.Service;

import java.util.HashMap;
import java.util.List;

public class MoJoObjective extends AbstractObjectiveFunction {

    public MoJoObjective() {
        this.objectiveFunctionTitle = "MoJo Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        // 源模块化：初始一个簇；目标模块化是：当前的modularAllele；
        // 由于源模块化只有一个点，因此最后只会产生一条连线
        // 也就是把所有的FA都已到一个G里面去
        // 然后在move时，把其他G的FA移动回去；所以move的次数就是：faNums - max（moduleFAMap.values.size）
        // 也不存在join操作
        HashMap<Integer, List<Integer>> moduleFAMap = Service.splitModularAlleleToServiceFAMap(chromosome);
        int maxModuleFASize = moduleFAMap.values().stream()
                .mapToInt(List::size).max().getAsInt();

        // 取MoJo的最大化
//        System.out.println("mojo: " + (1 - (PreProcessLoadData.faNums - maxModuleFASize) * 1.0 / PreProcessLoadData.faNums));
        return 1 - (PreProcessLoadData.faNums - maxModuleFASize) * 1.0 / PreProcessLoadData.faNums;
    }

}
