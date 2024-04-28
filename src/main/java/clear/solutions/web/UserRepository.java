package clear.solutions.web;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The assignment states that the data persistence layer is not required.
 * However, I decided to introduce the repository to simplify testing.
 */
@Repository
public class UserRepository {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public void save(User user) {
        if (user.id == null) user.id = UUID.randomUUID();
        users.put(user.id, user);
    }

    public User findById(UUID id) {
        return users.get(id);
    }

    public void delete(UUID id) {
        users.remove(id);
    }

    public List<User> findByBirthday(LocalDate from, LocalDate to) {
        return users.values().stream()
                .filter(user -> !user.birthday.isBefore(from) && !user.birthday.isAfter(to))
                .sorted(Comparator.comparing(user -> user.birthday))
                .toList();
    }

    public void clear() {
        users.clear();
    }
}
