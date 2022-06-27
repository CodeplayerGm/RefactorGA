package nsga.datastructure.excel;

import com.alibaba.excel.annotation.ExcelProperty;

public class ServiceData {
    @ExcelProperty(index = 0)
    private String name;
    @ExcelProperty(index = 1)
    private String path;

    public ServiceData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
