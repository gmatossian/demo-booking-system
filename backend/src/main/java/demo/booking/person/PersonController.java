package demo.booking.person;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PersonController {

    private final PersonRepository people;

    PersonController(PersonRepository people) {
        this.people = people;
    }

    @GetMapping("/api/people")
    List<Person> list() {
        return people.findAll();
    }
}
