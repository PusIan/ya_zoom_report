package service;

import java.util.List;

public interface IReader {
    List<String> readParticipantsFile();

    List<List<String>> readZoomReports();
}
