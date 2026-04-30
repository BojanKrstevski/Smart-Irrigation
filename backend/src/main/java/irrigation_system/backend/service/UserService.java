package irrigation_system.backend.service;

import irrigation_system.backend.dto.RegisterRequest;
import irrigation_system.backend.model.User;

public interface UserService {

    User register(RegisterRequest request);

    User findByEmail(String email);

    User findById(Long id);
}
