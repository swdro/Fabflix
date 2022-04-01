USE moviedb;

DROP TABLE IF EXISTS stars_in_movies;
DROP TABLE IF EXISTS genres_in_movies;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS creditcards;
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS stars;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS employees;
DROP PROCEDURE IF EXISTS add_movie;

CREATE TABLE movies (
	id varchar(10) DEFAULT '' NOT NULL ,
	title varchar(100) DEFAULT '' NOT NULL,
	year integer NOT NULL,
	director varchar(100) DEFAULT '' NOT NULL,
	PRIMARY KEY (id),
	FULLTEXT (title)
);

CREATE TABLE stars (
	id varchar(10) DEFAULT '' NOT NULL,
	name varchar(100) DEFAULT '' NOT NULL,
	birthYear integer DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE stars_in_movies (
	starId varchar(10) DEFAULT '' NOT NULL,
    movieId varchar(10) DEFAULT '' NOT NULL,
    FOREIGN KEY(starId) REFERENCES stars(id) ON DELETE CASCADE,
    FOREIGN KEY(movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE genres (
	id integer NOT NULL AUTO_INCREMENT,
	name varchar(32) DEFAULT '' NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE genres_in_movies (
	genreId integer NOT NULL,
	movieId varchar(10) DEFAULT '' NOT NULL,
	FOREIGN KEY(genreId) REFERENCES genres(id) ON DELETE CASCADE,
    FOREIGN KEY(movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE creditcards (
	id varchar(20) DEFAULT '' NOT NULL,
    firstName varchar(50) DEFAULT '' NOT NULL,
	lastName varchar(50) DEFAULT '' NOT NULL,
	expiration date NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE customers (
	id integer NOT NULL AUTO_INCREMENT,
	firstName varchar(50) DEFAULT '' NOT NULL,
	lastName varchar(50) DEFAULT '' NOT NULL,
	ccId varchar(20) DEFAULT '' NOT NULL,
	address varchar(200) DEFAULT '' NOT NULL,
	email varchar(50) DEFAULT '' NOT NULL,
	password varchar(20) DEFAULT '' NOT NULL,
    FOREIGN KEY(ccId) REFERENCES creditcards(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE sales (
	id integer NOT NULL AUTO_INCREMENT,
    customerId integer NOT NULL,
    movieId varchar(10) DEFAULT '' NOT NULL,
    saleDate date NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(customerId) REFERENCES customers(id) ON DELETE CASCADE,
	FOREIGN KEY(movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE ratings (
	movieId varchar(10) DEFAULT '' NOT NULL,
	rating float NOT NULL,
	numVotes integer NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE employees (
	email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);











