package nsga.datastructure;

import java.util.ArrayList;
import java.util.List;

public class GroupItemAllele extends AbstractAllele {

    private List<FunctionalAtom> faList;

    public GroupItemAllele (List<FunctionalAtom> faList) {
        super(faList);
        this.faList = faList;
    }

    @Override
    public List<FunctionalAtom> getGene() {
        return faList;
    }

    @Override
    public AbstractAllele getCopy() {
        return new GroupItemAllele(new ArrayList<>(this.faList));
    }

    public List<String> getAllFiles() {
        List<String> allFiles = new ArrayList<>();
        for (FunctionalAtom fa : faList) {
            allFiles.addAll(fa.fileList);
        }
        return allFiles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupItemAllele) {
            GroupItemAllele contrast = (GroupItemAllele) obj;
            if (this.faList.size() != contrast.faList.size()) {
                return false;
            }
            for (FunctionalAtom fa : this.faList) {
                if (!contrast.faList.contains(fa)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 返回一个固定值，强行执行equals
        return 0;
    }
}
