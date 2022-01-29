package nsga.datastructure;

import java.util.ArrayList;
import java.util.List;

public class ModularAllele extends AbstractAllele {

    private final List<Integer> moduleIdList;

    public ModularAllele(List<Integer> moduleIdList) {
        super(moduleIdList);
        this.moduleIdList = moduleIdList;
    }

    @Override
    public List<Integer> getGene() {
        return moduleIdList;
    }

    @Override
    public ModularAllele getCopy() {
        return new ModularAllele(new ArrayList<>(moduleIdList));
    }
}
