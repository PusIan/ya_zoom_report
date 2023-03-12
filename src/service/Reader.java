package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Reader implements IReader {
    private final String zoomReportFolder;
    private final String participantsFileName;

    public Reader(String zoomReportFolder, String participantsFileName) {
        this.zoomReportFolder = zoomReportFolder;
        this.participantsFileName = participantsFileName;
    }

    private static List<String> readFileContents(String fileName) {
        try {
            return new ArrayList<>(Files.readAllLines(Path.of(fileName)));
        } catch (IOException e) {
            throw new RuntimeException("Can't read file " + fileName);
        }
    }

    private static File[] getCsvFileList(String path) {
        File home = new File(path);
        if (home.isFile()) {
            throw new RuntimeException("Path must be directory");
        }
        File[] files = home.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files.length == 0) {
            throw new RuntimeException("Path " + path + " does not contain csv files");
        }
        return files;
    }

    @Override
    public List<String> readParticipantsFile() {
        return readFileContents(participantsFileName);
    }

    @Override
    public List<List<String>> readZoomReports() {
        List<List<String>> listList = new LinkedList<>();
        File[] files = getCsvFileList(this.zoomReportFolder);
        for (File file : files) {
            listList.add(readFileContents(file.getAbsolutePath()));
        }
        return listList;
    }
}
