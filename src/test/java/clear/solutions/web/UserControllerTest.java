package clear.solutions.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.clear();
    }

    @Test
    void testSuccessfulPost() throws Exception {
        var request = post("/api/users")
                .content("""
                        {
                        "email": "test@example.com",
                        "firstName": "Test",
                        "lastName": "Testov",
                        "birthday": "2000-04-27",
                        "address": "Dnipro",
                        "phoneNumber": "+380-12-345-6789"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testInvalidEmailPost() throws Exception {
        var request = post("/api/users")
                .content("""
                        {
                        "email": "testexample.com",
                        "firstName": "Test",
                        "lastName": "Testov",
                        "birthday": "2000-04-27"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testEmptyFirstNamePost() throws Exception {
        var request = post("/api/users")
                .content("""
                        {
                        "email": "test@example.com",
                        "firstName": "",
                        "lastName": "Testov",
                        "birthday": "2000-04-27"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testMissingLastNamePost() throws Exception {
        var request = post("/api/users")
                .content("""
                        {
                        "email": "test@example.com",
                        "firstName": "Test",
                        "birthday": "2000-04-27"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testNotAdultPost() throws Exception {
        LocalDate birthday = LocalDate.now().minusYears(10);
        var request = post("/api/users")
                .content(String.format("""
                        {
                        "email": "test@example.com",
                        "firstName": "Test",
                        "lastName": "Testov",
                        "birthday": "%s"
                        }
                        """, birthday))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testFutureDatePost() throws Exception {
        LocalDate birthday = LocalDate.now().plusYears(1);
        var request = post("/api/users")
                .content(String.format("""
                        {
                        "email": "test@example.com",
                        "firstName": "Test",
                        "lastName": "Testov",
                        "birthday": "%s"
                        }
                        """, birthday))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testUserNotFoundPatch() throws Exception {
        var request = patch("/api/users/{id}", UUID.randomUUID())
                .content("""
                        {
                        "firstName": "Dima",
                        "lastName": "Test"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void testRequiredFieldsPatch() throws Exception {
        User user = createUser("test@example.com", "Test", "Testov", LocalDate.of(2000, 5, 12));
        userRepository.save(user);
        var request = patch("/api/users/{id}", user.id)
                .content("""
                        {
                        "firstName": "Dima",
                        "birthday": "2002-04-27"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Dima"))
                .andExpect(jsonPath("$.birthday").value("2002-04-27"));
    }

    @Test
    void testOptionalFieldsPatch() throws Exception {
        User user = createUser("test@example.com", "Test", "Testov", LocalDate.of(2000, 5, 12));
        userRepository.save(user);
        var request = patch("/api/users/{id}", user.id)
                .content("""
                        {
                        "address": "Dnipro",
                        "phoneNumber": "+380-12-345-6789"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.address").value("Dnipro"))
                .andExpect(jsonPath("$.phoneNumber").value("+380-12-345-6789"));
    }

    @Test
    void testDelete() throws Exception {
        User user1 = createUser("test1@example.com", "Test1", "Testov1", LocalDate.of(2000, 5, 12));
        User user2 = createUser("test2@example.com", "Test2", "Testov2", LocalDate.of(2000, 5, 12));
        userRepository.save(user1);
        userRepository.save(user2);
        var request = delete("/api/users/{id}", user1.id);

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertNull(userRepository.findById(user1.id));
        assertNotNull(userRepository.findById(user2.id));
    }

    @Test
    void testSuccessfulPut() throws Exception {
        User user = createUser("test@example.com", "Test", "Testov", LocalDate.of(2000, 5, 12));
        userRepository.save(user);
        var request = patch("/api/users/{id}", user.id)
                .content("""
                        {
                        "email": "test1@example.com",
                        "firstName": "Test1",
                        "lastName": "Testov1",
                        "birthday": "2002-05-12",
                        "address": "Dnipro",
                        "phoneNumber": "+380-12-345-6789"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test1"))
                .andExpect(jsonPath("$.lastName").value("Testov1"))
                .andExpect(jsonPath("$.birthday").value("2002-05-12"))
                .andExpect(jsonPath("$.address").value("Dnipro"))
                .andExpect(jsonPath("$.phoneNumber").value("+380-12-345-6789"));
    }

    @Test
    void testInvalidPut() throws Exception {
        User user = createUser("test@example.com", "Test", "Testov", LocalDate.of(2000, 5, 12));
        userRepository.save(user);
        var request = patch("/api/users/{id}", user.id)
                .content("""
                        {
                        "email": ""
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void testFoundTwoUsersByBirthday() throws Exception {
        User user1 = createUser("test1@example.com", "Test1", "Testov1", LocalDate.of(2001, 5, 12));
        User user2 = createUser("test2@example.com", "Test2", "Testov2", LocalDate.of(2002, 5, 12));
        User user3 = createUser("test3@example.com", "Test3", "Testov3", LocalDate.of(2003, 5, 12));
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        var request = get("/api/users")
                .param("from", "2000-01-01")
                .param("to", "2002-12-31")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(user1.id.toString()))
                .andExpect(jsonPath("$.[1].id").value(user2.id.toString()));
    }

    @Test
    void testFoundEmptyUsersByBirthday() throws Exception {
        User user1 = createUser("test1@example.com", "Test1", "Testov1", LocalDate.of(2001, 5, 12));
        User user2 = createUser("test2@example.com", "Test2", "Testov2", LocalDate.of(2002, 5, 12));
        User user3 = createUser("test3@example.com", "Test3", "Testov3", LocalDate.of(2003, 5, 12));
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        var request = get("/api/users")
                .param("from", "2015-01-01")
                .param("to", "2020-12-31")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    private User createUser(String email, String firstName, String lastName, LocalDate birthday) {
        User user = new User();
        user.email = email;
        user.firstName = firstName;
        user.lastName = lastName;
        user.birthday = birthday;
        user.address = "";
        user.phoneNumber = "";
        return user;
    }
}