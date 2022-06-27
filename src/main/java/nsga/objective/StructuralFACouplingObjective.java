package nsga.objective;

import nsga.Service;
import nsga.datastructure.Chromosome;

import java.util.HashMap;
import java.util.List;

import static nsga.objective.SMQFAObjective.getAllServiceCouplingAsc;

/**
 * 结构性耦合目标：SMQ的耦合部分
 * 归一化，并转换为lack of coupling: 1 - coup，越大越好
 * FA Search 粒度
 */
public class StructuralFACouplingObjective extends AbstractObjectiveFunction {

    public StructuralFACouplingObjective() {
        this.objectiveFunctionTitle = "Structural FA Coupling Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFAMap = Service.splitModularAlleleToServiceFAMap(chromosome);
        return getAllServiceCouplingAsc(srvFAMap);
    }
}
