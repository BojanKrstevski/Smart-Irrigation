package irrigation_system.service;

import irrigation_system.dto.RegisterRequest;
import irrigation_system.model.User;

public interface UserService {

    User register(RegisterRequest request);

    User findByEmail(String email);

    User findById(Long id);
}
