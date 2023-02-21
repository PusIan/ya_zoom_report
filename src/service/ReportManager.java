package service;

import model.Conference;
import model.User;
import model.Visit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportManager implements IReportManager {
    private final Set<User> users;
    private final Map<Conference, Map<User, Visit>> userVisits;
    private final String DELIMITER = ",";

    public ReportManager(IDataProvider dataProvider) {
        this.users = dataProvider.getUsers();
        this.userVisits = dataProvider.getConferencesVisits();
    }

    @Override
    public Collection<String> generateReport() {
        Collection<String> lines = new LinkedList<>();
        lines.add(generateHeader());
        lines.addAll(generateBody());
        lines.add(generateTail());
        return lines;
    }

    private Collection<String> generateBody() {
        Collection<String> lines = new LinkedList<>();
        Collection<Conference> conferences = getConferenceList();
        Collection<Double> durationFraction;
        for (User user : this.users) {
            StringBuilder lineStringBuilder = new StringBuilder();
            lineStringBuilder.append(user.email());
            lineStringBuilder.append(DELIMITER);
            lineStringBuilder.append(user.name());
            lineStringBuilder.append(DELIMITER);
            durationFraction = new LinkedList<>();
            for (Conference conference : conferences) {
                if (this.userVisits.get(conference).containsKey(user)) {
                    int duration = this.userVisits.get(conference).get(user).duration();
                    lineStringBuilder.append(duration);
                    durationFraction.add(duration / Double.valueOf(conference.duration()));
                }
                lineStringBuilder.append(DELIMITER);
            }
            lineStringBuilder.append(durationFraction.size());
            lineStringBuilder.append(DELIMITER);
            lineStringBuilder.append(durationFraction.stream().mapToDouble(Double::doubleValue).average().orElseGet(() -> 0));
            lines.add(lineStringBuilder.toString());
        }
        return lines;
    }

    private Collection<Conference> getConferenceList() {
        return this.userVisits.keySet().stream().sorted().collect(Collectors.toList());
    }

    private String generateHeader() {
        Collection<String> listOfConferences = getConferenceList().stream().map(Conference::toString).toList();
        return DELIMITER + DELIMITER + String.join(DELIMITER, listOfConferences);
    }

    private String generateTail() {
        Collection<Conference> listOfConferences = getConferenceList();
        StringBuilder result = new StringBuilder();
        result.append(DELIMITER + DELIMITER);
        int totalNumberOfUsers = this.users.size();
        for (Conference conference : listOfConferences) {
            int totalNumberOfParticipants = this.userVisits.get(conference).keySet().size();
            result.append(totalNumberOfParticipants);
            result.append(" - ");
            result.append(totalNumberOfParticipants / Double.valueOf(totalNumberOfUsers));
            result.append(DELIMITER);
        }
        return result.toString();
    }
}
