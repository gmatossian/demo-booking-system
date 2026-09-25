package demo.booking.person;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepository {

    private final JdbcClient jdbc;

    PersonRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Person> findAll() {
        return jdbc.sql("SELECT id, display_name FROM person ORDER BY display_name")
                .query((rs, rowNum) -> new Person(rs.getLong("id"), rs.getString("display_name")))
                .list();
    }

    public boolean existsById(long id) {
        return jdbc.sql("SELECT COUNT(*) FROM person WHERE id = :id")
                .param("id", id)
                .query(Integer.class)
                .single() > 0;
    }

    public void insert(Person person) {
        jdbc.sql("INSERT INTO person (id, display_name) VALUES (:id, :displayName)")
                .param("id", person.id())
                .param("displayName", person.displayName())
                .update();
    }

    public void deleteAll() {
        jdbc.sql("DELETE FROM person").update();
    }
}
