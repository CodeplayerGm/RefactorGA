package nsga.datastructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionalAtom {
    public List<String> fileList;

    public FunctionalAtom(List<String> fileList) {
        this.fileList = fileList;
    }

    public FunctionalAtom(FunctionalAtom fa) {
        this.fileList = new ArrayList<>(fa.fileList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionalAtom) {
            FunctionalAtom contrast = (FunctionalAtom) obj;
            if (this.fileList.size() != contrast.fileList.size()) {
                return false;
            }
            // 主要是对比代码文件是否一致
            for (String file : this.fileList) {
                if (!contrast.fileList.contains(file)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 将fileList排序后的字符串的hashcode作为计算依据
        String[] fileArr = fileList.toArray(new String[0]);
        Arrays.sort(fileArr);
        StringBuilder sb = new StringBuilder();
        for (String file : fileArr) {
            sb.append(file);
        }
        return sb.toString().hashCode();
    }
}
