package service;

import model.Conference;
import model.Email;
import model.User;
import model.Visit;
import model.csv.UserVisitRaw;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataProvider implements IDataProvider {
    private final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private final SimpleDateFormat parser = new SimpleDateFormat(DATE_FORMAT);
    private final int FIELD_CNT_IN_CONF_LINE = 7;
    private final int FIELD_CNT_IN_VISIT_LINE = 4;
    private final int FIELD_CNT_IN_USER_LINE = 2;
    private final int CONF_LINE_IDX_START = 1;
    private final int VISIT_LINE_IDX_START = 4;
    private final String REGEX_DELIMITER = "\\,";
    private final String zoomReportFolder;
    private final String participantsFileName;

    private Set<User> users;
    private Map<Conference, Map<User, Visit>> userVisits;

    public DataProvider(String zoomReportFolder, String participantsFileName) {
        this.zoomReportFolder = zoomReportFolder;
        this.participantsFileName = participantsFileName;
    }

    public static Collection<String> readFileContents(String fileName, int startLine, int numberOfLines) {
        return readFileContents(fileName, startLine)
                .stream()
                .limit(numberOfLines)
                .collect(Collectors.toList());
    }

    public static Collection<String> readFileContents(String fileName, int startLine) {
        return readFileContents(fileName)
                .stream()
                .skip(startLine)
                .collect(Collectors.toList());
    }

    public static Collection<String> readFileContents(String fileName) {
        try {
            return new ArrayList<>(Files.readAllLines(Path.of(fileName)));
        } catch (IOException e) {
            throw new RuntimeException("Can't read file " + fileName);
        }
    }

    private Set<User> parseUserFile(String fileName) {
        Collection<String> fileContent = readFileContents(fileName);
        Set<User> users = new LinkedHashSet<>();
        for (String s : fileContent) {
            String[] splittedLine = s.split(REGEX_DELIMITER);
            if (splittedLine.length != FIELD_CNT_IN_USER_LINE) {
                throw new RuntimeException("Participants file format is invalid, must contain " + FIELD_CNT_IN_USER_LINE + " fields, divided by ','");
            }
            Email email = new Email(splittedLine[1]);
            User user = new User(email, splittedLine[0]);
            if (users.contains(user)) {
                throw new RuntimeException("Duplication with email in participants file, email: " + user.email());
            }
            users.add(user);
        }
        return users;
    }

    private Map<Conference, Map<User, Visit>> parseConferenceFiles(String path) {
        File[] files = getCsvFileList(path);
        HashMap<Conference, Map<User, Visit>> userVisits = new HashMap<>();
        for (File file : files) {
            String conferenceLine = readFileContents(file.getAbsolutePath(), CONF_LINE_IDX_START, 1).stream().findFirst().get();
            Conference conference = parseConferenceLine(conferenceLine);
            if (userVisits.containsKey(conference)) {
                throw new RuntimeException("Duplications in conference id: " + conference.id());
            }
            userVisits.put(conference, new HashMap<>());
            Collection<String> visitLines = readFileContents(file.getAbsolutePath(), VISIT_LINE_IDX_START);
            for (String visitLine : visitLines) {
                UserVisitRaw userVisitRaw = parseUserVisit(visitLine);
                Email email = new Email(userVisitRaw.email());
                User user = new User(email, userVisitRaw.name());
                if (userVisits.get(conference).containsKey(user)) {
                    throw new RuntimeException("Duplications for user email: " + user.email() + "in conference id " + conference.id());
                }
                Visit visit = new Visit(userVisitRaw.guest(), userVisitRaw.duration());
                userVisits.get(conference).put(user, visit);
            }
        }
        return userVisits;
    }

    private Conference parseConferenceLine(String conferenceLine) {
        String[] splittedConferenceLine = conferenceLine.split(REGEX_DELIMITER);
        if (splittedConferenceLine.length != FIELD_CNT_IN_CONF_LINE) {
            throw new RuntimeException("Conference description format is invalid, must contain " + FIELD_CNT_IN_CONF_LINE + " fields, divided by ','");
        }
        Date dateBegin;
        Date dateEnd;
        try {
            dateBegin = parser.parse(splittedConferenceLine[2]);
            dateEnd = parser.parse(splittedConferenceLine[3]);
        } catch (ParseException e) {
            throw new RuntimeException("Current DataFormat " + DATE_FORMAT + "Invalid data format, please check validness: "
                    + splittedConferenceLine[2] + " or " + splittedConferenceLine[3]);
        }

        Conference conference = new Conference(Long.parseLong(splittedConferenceLine[0]),
                splittedConferenceLine[1], dateBegin, dateEnd, new Email(splittedConferenceLine[4]),
                Integer.parseInt(splittedConferenceLine[5]), Integer.parseInt(splittedConferenceLine[6]));
        return conference;
    }

    private UserVisitRaw parseUserVisit(String userVisitLine) {
        String[] splittedUserVisitLine = userVisitLine.split(REGEX_DELIMITER);
        if (splittedUserVisitLine.length != FIELD_CNT_IN_VISIT_LINE) {
            throw new RuntimeException("User visit line is invalid, must contain " + FIELD_CNT_IN_VISIT_LINE + " fields, divided by ','");
        }
        return new UserVisitRaw(splittedUserVisitLine[0],
                splittedUserVisitLine[1],
                Integer.parseInt(splittedUserVisitLine[2]),
                this.parseBoolean(splittedUserVisitLine[3]));
    }

    private boolean parseBoolean(String str) {
        str = str.toLowerCase();
        return str.equals("yes") || str.equals("true") || str.equals("да");
    }

    private File[] getCsvFileList(String path) {
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
    public Set<User> getUsers() {
        if (this.users == null) {
            this.users = this.parseUserFile(this.participantsFileName);
        }
        return this.users;
    }

    @Override
    public Map<Conference, Map<User, Visit>> getConferencesVisits() {
        if (this.userVisits == null) {
            this.userVisits = this.parseConferenceFiles(this.zoomReportFolder);
        }
        return this.userVisits;
    }
}
