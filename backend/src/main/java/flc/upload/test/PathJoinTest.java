package flc.upload.test;

import flc.upload.util.FileUtil;

import java.io.File;
import java.nio.file.Paths;

public class PathJoinTest {

    public static void main(String[] args) {
        File file = FileUtil.getFile("D:/", "Projects", "/upload", "src");
        //File file = Paths.get("D:/", "Projects", "/upload", "src").toFile();
        System.out.println(file.getAbsolutePath());

    }
}
