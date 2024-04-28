package clear.solutions.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    public UUID id;

    @Email(message = "invalid email")
    @NotBlank(message = "email is required")
    public String email;

    @NotBlank(message = "firstName is required")
    public String firstName;

    @NotBlank(message = "lastName is required")
    public String lastName;

    @NotNull(message = "birthday is required")
    @Past(message = "date of birth must be earlier than current date")
    public LocalDate birthday;

    public String address;

    public String phoneNumber;

    public User() {
    }

    public User(User user) {
        id = user.id;
        email = user.email;
        firstName = user.firstName;
        lastName = user.lastName;
        birthday = user.birthday;
        address = user.address;
        phoneNumber = user.phoneNumber;
    }
}
