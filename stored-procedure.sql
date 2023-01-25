use moviedb;

DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie (newTitle text, newYear integer, newDirector varchar(100), genre varchar(32), star varchar(100), OUT message varchar(100))
BEGIN
    DECLARE newMovieId varchar(9);
    DECLARE newStarId varchar(9);
    DECLARE newGenreId int;

    DROP TABLE IF EXISTS logTable;
    CREATE TABLE logTable (logOutput varchar(100));

    IF ((select count(*) from movies where title = newTitle AND director = newDirector AND year = newYear) > 0)
        THEN
        INSERT INTO logTable(logOutput) values('Movie already in database');
        SET message = 'exists';

    ELSE
        SET newMovieId = concat('tt', convert((CONVERT((SELECT SUBSTRING((select MAX(id) from movies), 3)), unsigned integer) + 1), CHAR(10)));
        INSERT INTO movies(id, title, year, director) VALUES (newMovieId, newTitle, newYear, newDirector);
        INSERT INTO logTable(logOutput) VALUES ('New Movie Added');

        IF((select count(*) from genres where name = genre) > 0) THEN
			SET newGenreId = (select id from genres where name = genre);
			INSERT INTO genres_in_movies(genreId, movieId) VALUES (newGenreId, newMovieId);
			INSERT INTO logTable VALUES ('genre already exists, linked genre to movie (genre_in_movies)');
		ELSE
		    SET newGenreId = (select MAX(id) from genres) + 1;
		    INSERT INTO genres(id, name) VALUES (newGenreId, genre);
            INSERT INTO genres_in_movies(genreId, movieId) values(newGenreId, newMovieId);
		    INSERT INTO logTable VALUES ('added new genre (genres), linked genre to movie (genre_in_movies)');
        END IF;

        IF((select count(*) from stars where name = star) > 0) THEN
            SET newStarId = (select id from stars where name = star);
            INSERT INTO stars_in_movies(starId, movieId) values(newStarId, newMovieId);
            INSERT INTO logTable VALUES ('star already exists, linked star to movie (stars_in_movies)');
        ELSE
            SET newStarId = concat('nm', convert((CONVERT((SELECT SUBSTRING((select MAX(id) from movies), 3)), unsigned integer) + 1), CHAR(10)));
            INSERT INTO stars(id, name) VALUES(newStarId, star);
            INSERT INTO stars_in_movies(starId, movieId) VALUES(newStarId, newMovieId);
            INSERT INTO logTable VALUES ('added new star (stars), linked star to movie (stars_in_movies)');
		END IF;

        SET message = 'Successfully Added Movie';

    END IF;

    SELECT * FROM logTable;
    DROP TABLE IF EXISTS logTable;

END
$$

DELIMITER ;