package util;

import java.io.File;

public class FileUtil {
    /**
     * Check the existence of the folders. If it doesn't exist, it will be created.
     */
    public static void checkFolder() {
        File detailFolder = new File(PropertyUtil.DETAIL_FOLDER_PATH);
        if (!detailFolder.exists()) {
            detailFolder.mkdirs();
        }
    }
}
