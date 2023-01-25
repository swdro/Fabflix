import java.util.Locale;
import java.util.Objects;

public class Movie {

    private final String title;

    private final Integer year;

    private final String director;

    public Movie(String title, Integer year, String director) {
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", director='" + director + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return title.equalsIgnoreCase(movie.title) && year.equals(movie.year) && director.equalsIgnoreCase(movie.director);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title.trim().toLowerCase(Locale.ROOT), year, director.trim().toLowerCase(Locale.ROOT));
    }
}
