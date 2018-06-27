package util;

import java.io.File;

public class FileUtil {
    /**
     * Check the existence of the folders. If it doesn't exist, it will be created.
     */
    public static void checkFolder() {
        File resultFolder = new File(PropertyUtil.RESULT_FOLDER_PATH);
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }
        File detailFolder = new File(PropertyUtil.DETAIL_FOLDER_PATH);
        if (!detailFolder.exists()) {
            detailFolder.mkdirs();
        }
        File costFolder = new File(PropertyUtil.COST_FOLDER_PATH);
        if (!costFolder.exists()) {
            costFolder.mkdirs();
        }
    }
}
