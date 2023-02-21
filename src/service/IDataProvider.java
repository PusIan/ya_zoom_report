package service;

import model.Conference;
import model.User;
import model.Visit;

import java.util.Map;
import java.util.Set;

public interface IDataProvider {
    Set<User> getUsers();

    Map<Conference, Map<User, Visit>> getConferencesVisits();
}
