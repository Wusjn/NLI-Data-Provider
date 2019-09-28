package provider.utils;

import java.io.File;

public class DirExplorer {
    int fileProcessed = 0;

    public interface FileFilter {
        boolean interested(int level, String path, File file);
    }

    public interface FileHandler {
        void handle(int level, String path, File file);
    }

    private FileFilter fileFilter;
    private FileHandler fileHandler;

    public DirExplorer(FileFilter filter, FileHandler handler) {
        this.fileFilter = filter;
        this.fileHandler = handler;
    }

    public void explore(File root) {
        explore(0, "", root);
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (fileFilter.interested(level, path, file)) {
                fileHandler.handle(level, path, file);
                System.out.println(fileProcessed);
                fileProcessed += 1;
            }
        }
    }

}
