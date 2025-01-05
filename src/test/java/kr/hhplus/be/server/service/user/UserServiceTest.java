package kr.hhplus.be.server.service.user;

import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("사용자 ID로 사용자 조회 성공")
    void getUserByIdSuccess() {
        User user = User.of().name("John Doe").email("john.doe@example.com").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserById(1L);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회 실패 - 사용자 없음")
    void getUserByIdNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotPresent();
    }
}