import java.util.Objects;

public class Star {

    private final String name;

    private final Integer year;

    public Star(String name, Integer year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "Star{" +
                "name='" + name + '\'' +
                ", year=" + year +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return name.equals(star.name) && Objects.equals(year, star.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, year);
    }
}
