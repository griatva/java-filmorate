package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.genre.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.mpa.JdbcMpaRepository;
import ru.yandex.practicum.filmorate.repository.user.JdbcUserRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;



@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcUserRepository.class, JdbcFilmRepository.class, JdbcGenreRepository.class, JdbcMpaRepository.class})
class FilmorateApplicationTests {

	private final JdbcFilmRepository filmRepository;
	private final JdbcUserRepository userRepository;
	private final JdbcGenreRepository genreRepository;
	private final JdbcMpaRepository mpaRepository;
	private final JdbcTemplate jdbc;


	@Test
	void contextLoads() {
		assertNotNull(userRepository);
		assertNotNull(filmRepository);
		assertNotNull(genreRepository);
		assertNotNull(mpaRepository);
		assertNotNull(jdbc);
	}

	@AfterEach
	public void resetAutoIncrement() {
		jdbc.execute("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
		jdbc.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
	}

	private Film createNewFilm(Long id, String name, String description, LocalDate releaseDate,
							   int duration, RatingMPA mpa) {

		LinkedHashSet<Genre> genres = new LinkedHashSet<>();
		Genre genre1 = new Genre();
		genre1.setId(1);
		genre1.setName("Комедия");
		genres.add(genre1);

		Genre genre2 = new Genre();
		genre2.setId(2);
		genre2.setName("Драма");
		genres.add(genre2);

		Film film = new Film();
		film.setId(id);
		film.setName(name);
		film.setDescription(description);
		film.setReleaseDate(releaseDate);
		film.setDuration(duration);
		film.setGenres(genres);
		film.setMpa(mpa);
		film.setLikes(0);
		return film;
	}

	private User createNewUser(Long id, String email, String login,
							   String name, LocalDate birthday) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setLogin(login);
		user.setName(name);
		user.setBirthday(birthday);
		user.setFriendsIds(new LinkedHashSet<>());
		return user;
	}

	@Test
	@Transactional
	public void getFilmsList() {
		//given
		Film film1 = createNewFilm(null, "Name1", "Description1", 
				LocalDate.of(2020, 11, 11),
				110, new RatingMPA(1, "G"));
		Film film2 = createNewFilm(null, "Name2", "Description2", 
				LocalDate.of(2020, 12, 12),
				120, new RatingMPA(2, "PG"));

		filmRepository.createFilm(film1);
		filmRepository.createFilm(film2);

		List<Film> filmListExpected = new ArrayList<>();
		filmListExpected.add(film1);
		filmListExpected.add(film2);

		//when
		List<Film> filmListActual = filmRepository.getFilmsList();

		//then
		assertThat(filmListActual).containsExactlyInAnyOrderElementsOf(filmListExpected);

	}

	@Test
	@Transactional
	public void createFilm() {
		//given
		Film filmExpected = createNewFilm(null, "Name1", "Description1", 
				LocalDate.of(2020, 11, 11), 110, new RatingMPA(1, "G"));

		//when
		Film filmActual = filmRepository.createFilm(filmExpected);

		//then
		assertThat(filmActual).isEqualTo(filmExpected);
	}

	@Test
	@Transactional
	public void updateFilm() {
		//given
		Film filmOld = createNewFilm(null, "Name1", "Description1",
				LocalDate.of(2020, 11, 11),110, new RatingMPA(1, "G"));
		filmRepository.createFilm(filmOld);
		Film filmExpected = createNewFilm(1L, "Name2", "Description2",
				LocalDate.of(2020, 12, 12),120, new RatingMPA(2, "PG"));

		//when
		filmRepository.updateFilm(filmExpected);
		Optional<Film> filmActual = filmRepository.findFilmById(1);

		//then
		assertThat(filmActual)
				.isPresent()
				.get()
				.isEqualTo(filmExpected);
	}

	@Test
	@Transactional
	public void addLike() {
		//given
		User user = createNewUser(null, "ivan@mail.ru", "ivan1985", "Ivan",
				LocalDate.of(1985, 10, 10));
		Film film1 = createNewFilm(null, "Name1", "Description1",
				LocalDate.of(2020, 11, 11),110, new RatingMPA(1, "G"));
		Film film2 = createNewFilm(null, "Name2", "Description2",
				LocalDate.of(2020, 12, 12),120, new RatingMPA(2, "PG"));

		userRepository.createUser(user);
		filmRepository.createFilm(film1);
		filmRepository.createFilm(film2);

		// when
		filmRepository.addLike(2, 1);

		// then
		String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
		Integer count = jdbc.queryForObject(sql, Integer.class, 2, 1);
		assertThat(count).isEqualTo(1);

		Film film = filmRepository.findFilmById(2).orElseThrow(() ->
				new NotFoundException("Фильм, которому поставили лайки, не найден"));
		assertEquals(1, film.getLikes(), "Количество лайков неверное, лайк не добавился");
	}

	@Test
	@Transactional
	public void deleteLike() {
		//given
		User user = createNewUser(null, "ivan@mail.ru", "ivan1985", "Ivan",
				LocalDate.of(1985, 10, 10));
		Film film1 = createNewFilm(null, "Name1", "Description1",
				LocalDate.of(2020, 11, 11),110, new RatingMPA(1, "G"));
		Film film2 = createNewFilm(null, "Name2", "Description2",
				LocalDate.of(2020, 12, 12),120, new RatingMPA(2, "PG"));

		userRepository.createUser(user);
		filmRepository.createFilm(film1);
		filmRepository.createFilm(film2);
		filmRepository.addLike(2, 1);

		//when
		filmRepository.deleteLike(2, 1);

		// then
		String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
		Integer count = jdbc.queryForObject(sql, Integer.class, 2, 1);
		assertThat(count).isEqualTo(0);

	}

	@Test
	@Transactional
	public void getPopularFilms() {

		//given
		Film film1 = createNewFilm(null, "Name1", "Description1",
				LocalDate.of(2020, 11, 11),110, new RatingMPA(1, "G"));
		Film film2 = createNewFilm(null, "Name2", "Description2",
				LocalDate.of(2020, 12, 12),120, new RatingMPA(2, "PG"));
		Film film3 = createNewFilm(null, "Name3", "Description3",
				LocalDate.of(2020, 12, 13),130, new RatingMPA(3, "PG-13"));
		filmRepository.createFilm(film1);
		filmRepository.createFilm(film2);
		filmRepository.createFilm(film3);

		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		filmRepository.addLike(1, 1);
		filmRepository.addLike(2, 1);
		filmRepository.addLike(2, 2);
		filmRepository.addLike(3, 1);
		filmRepository.addLike(3, 2);
		filmRepository.addLike(3, 3);

		//when
		List<Film> mostPopularFilmList = filmRepository.getPopularFilms(2);

		// then
		assertThat(mostPopularFilmList)
				.hasSize(2)
				.extracting(Film::getId)
				.containsExactly(3L, 2L);
	}

	@Test
	@Transactional
	public void findFilmById() {

		Film expectedFilm = createNewFilm(null, "Name1", "Description1", 
				LocalDate.of(2020, 11, 11),
				110, new RatingMPA(1, "G"));

		filmRepository.createFilm(expectedFilm);

		Optional<Film> filmOptional = filmRepository.findFilmById(1);

		assertThat(filmOptional)
				.isPresent()
				.get()
				.isEqualTo(expectedFilm);
	}

	@Test
	@Transactional
	public void getUserList() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);

		List<User> userListExpected = new ArrayList<>();
		userListExpected.add(user1);
		userListExpected.add(user2);

		//when
		List<User> userListActual = userRepository.getUserList();

		//then
		assertThat(userListActual).containsExactlyInAnyOrderElementsOf(userListExpected);
	}


	@Test
	@Transactional
	public void createUser() {
		//given
		User userExpected = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));

		//when
		User userActual = userRepository.createUser(userExpected);

		//then
		assertThat(userActual).isEqualTo(userExpected);
	}


	@Test
	@Transactional
	public void updateUser() {
		//given
		User userOld = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		userRepository.createUser(userOld);
		User userExpected = createNewUser(1L, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));

		//when
		userRepository.updateUser(userExpected);
		User userActual = userRepository.getUserList().getFirst();

		//then
		assertThat(userActual).isEqualTo(userExpected);
	}

	@Test
	@Transactional
	public void addFriend_OneSidedFriendshipIsCreated_AtTheFirstRequest() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		//when
		userRepository.addFriend(1, 3);

		//then
		Integer count1To3 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(count1To3).isEqualTo(1);

		Integer status1To3 = jdbc.queryForObject(
				"SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(status1To3).isEqualTo(2);

		Integer count3To1 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 3, 1);
		assertThat(count3To1).isEqualTo(0);
	}

	@Test
	@Transactional
	public void addFriend_mustEstablishTwoWayFriendship_uponConfirmation() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		//when
		userRepository.addFriend(1, 3);
		userRepository.addFriend(3, 1);

		//then
		Integer count1To3 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(count1To3).isEqualTo(1);

		Integer count3To1 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 3, 1);
		assertThat(count3To1).isEqualTo(1);

		Integer status1To3 = jdbc.queryForObject(
				"SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(status1To3).isEqualTo(1);

		Integer status3To1 = jdbc.queryForObject(
				"SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 3, 1);
		assertThat(status3To1).isEqualTo(1);
	}


	@Test
	@Transactional
	public void deleteFriend() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		userRepository.addFriend(1, 3);

		//when
		userRepository.deleteFriend(1, 3);

		// then
		Integer count1To3 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(count1To3).isEqualTo(0);


	}

	@Test
	@Transactional
	public void deleteFriend_shouldChangeTheStatus_ifThereWasTwoWayFriendship() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		userRepository.addFriend(1, 3);
		userRepository.addFriend(3, 1);

		//when
		userRepository.deleteFriend(1, 3);

		// then
		Integer count1To3 = jdbc.queryForObject(
				"SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 1, 3);
		assertThat(count1To3).isEqualTo(0);

		Integer status3To1 = jdbc.queryForObject(
				"SELECT status_id FROM friendship WHERE user_id = ? AND friend_id = ?",
				Integer.class, 3, 1);
		assertThat(status3To1).isEqualTo(2);

	}

	@Test
	@Transactional
	public void getFriendsList() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));

		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		userRepository.addFriend(1, 2);
		userRepository.addFriend(1, 3);

		//when
		List<User> friendsList = userRepository.getFriendsList(1);

		//then
		assertThat(friendsList)
				.hasSize(2)
				.containsExactlyInAnyOrder(user2, user3);
	}

	@Test
	@Transactional
	public void getCommonFriendsList() {
		//given
		User user1 = createNewUser(null, "ivan1@mail.ru", "ivan1985_1", "Ivan I",
				LocalDate.of(1985, 10, 10));
		User user2 = createNewUser(null, "ivan2@mail.ru", "ivan1985_2", "Ivan II",
				LocalDate.of(1986, 10, 10));
		User user3 = createNewUser(null, "ivan3@mail.ru", "ivan1985_3", "Ivan III",
				LocalDate.of(1987, 10, 10));


		userRepository.createUser(user1);
		userRepository.createUser(user2);
		userRepository.createUser(user3);

		userRepository.addFriend(1, 2);
		userRepository.addFriend(1, 3);
		userRepository.addFriend(2, 3);

		//when
		List<User> commonFriends = userRepository.getCommonFriendsList(1, 2);

		//then
		assertThat(commonFriends)
				.hasSize(1)
				.containsExactlyInAnyOrder(user3);
	}

	@Test
	@Transactional
	public void getAllMpa() {
		List<RatingMPA> mpaList = mpaRepository.getAllMpa();

		assertThat(mpaList)
				.hasSize(5)
				.extracting(RatingMPA::getName)
				.containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");

	}

	@Test
	@Transactional
	public void getMpaById() {
		RatingMPA ratingMPA = mpaRepository.getMpaById(2);
		assertThat(ratingMPA)
				.isNotNull()
				.extracting(RatingMPA::getName)
				.isEqualTo("PG");
	}

	@Test
	@Transactional
	public void getAllGenres() {
		List<Genre> genreList = genreRepository.getAllGenres();

		assertThat(genreList)
				.hasSize(6)
				.extracting(Genre::getName)
				.containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
	}


	@Test
	@Transactional
	public void getGenreById() {
		Genre genre = genreRepository.getGenreById(2);
		assertThat(genre)
				.isNotNull()
				.extracting(Genre::getName)
				.isEqualTo("Драма");
	}

}
