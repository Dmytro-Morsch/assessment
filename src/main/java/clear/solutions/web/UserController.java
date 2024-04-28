package clear.solutions.web;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {
    @Value("${min-age}")
    private int minAge;

    @Autowired
    private Validator validator;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    List<User> getUsers(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        if (from.isAfter(to)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value 'from' must be earlier than 'to' or equal");
        return userRepository.findByBirthday(from, to);
    }

    @PostMapping
    User postUser(@RequestBody User user) {
        validateUser(user);
        userRepository.save(user);
        return user;
    }

    @PatchMapping(path = "/{id}")
    User patchUser(@PathVariable UUID id, @RequestBody User patch) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        User user = new User(existingUser);
        if (patch.email != null) user.email = patch.email;
        if (patch.firstName != null) user.firstName = patch.firstName;
        if (patch.lastName != null) user.lastName = patch.lastName;
        if (patch.birthday != null) user.birthday = patch.birthday;
        if (patch.address != null) user.address = patch.address;
        if (patch.phoneNumber != null) user.phoneNumber = patch.phoneNumber;
        validateUser(user);
        userRepository.save(user);
        return user;
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable UUID id) {
        userRepository.delete(id);
    }

    @PutMapping(path = "/{id}")
    User putUser(@PathVariable UUID id, @RequestBody User user) {
        user.id = id;
        validateUser(user);
        userRepository.save(user);
        return user;
    }

    private void validateUser(User user) {
        if (!isAdult(user.birthday)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be adult");
        var violations = validator.validate(user);
        if (!violations.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user: " + violations);
    }

    private boolean isAdult(LocalDate birthday) {
        return birthday.plusYears(minAge).isBefore(LocalDate.now());
    }
}
