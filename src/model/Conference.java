package model;

import java.util.Date;
import java.util.Objects;

public record Conference(long id, String name, Date begin, Date end, Email email, int duration,
                         int numberOfParticipants) implements Comparable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conference that = (Conference) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.id() + " - " + this.begin();
    }

    @Override
    public int compareTo(Object o) {
        int compareToDateBegin = this.begin().compareTo(((Conference) o).begin());
        if (compareToDateBegin != 0) {
            return compareToDateBegin;
        }
        int compareToDateEnd = this.end().compareTo(((Conference) o).end());
        if (compareToDateEnd != 0) {
            return compareToDateEnd;
        }
        return this.name.compareTo(((Conference) o).name());
    }
}
