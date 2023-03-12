import service.*;

public class Main {
    private static String zoomReportFolder = "";
    private static String participantsFileName = "";

    public static void main(String[] args) {
        parseArgs(args);
        IReader reader = new Reader(zoomReportFolder, participantsFileName);
        IDataProvider dataProvider = new DataProvider(reader);
        IReportGenerator manager = new ReportGenerator(dataProvider);
        for (String line : manager.generateReport()) {
            System.out.println(line);
        }
    }

    private static void parseArgs(String[] args) {
        String help = "You must provide arguments: --data (the folder for zoom report files) and --participants (the file for users)";
        if (args.length != 4) {
            throw new IllegalArgumentException(help);
        } else {
            int i = 0;
            while (i < args.length) {
                switch (args[i]) {
                    case "--data":
                        zoomReportFolder = args[++i];
                        break;
                    case "--participants":
                        participantsFileName = args[++i];
                        break;
                    default:
                        throw new IllegalArgumentException(help);
                }
                i++;
            }
        }
        if (zoomReportFolder.isBlank() || participantsFileName.isBlank()) {
            throw new IllegalArgumentException(help);
        }
    }
}