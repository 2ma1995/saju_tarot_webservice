package service.saju_taro_service.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.dto.user.UserSignupRequest;
import service.saju_taro_service.dto.user.UserUpdateRequest;
import service.saju_taro_service.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(UserSignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일 입니다.");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .userRole(UserRole.USER)
                .build();
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

//    @Transactional(readOnly = true)
//    public UserResponse login(UserLoginRequest req) {
//        User user = userRepository.findByEmailAndIsActiveTrue(req.getEmail())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
//        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
//            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
//        }
//        return UserResponse.fromEntity(user);
//    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id){
        return userRepository.findById(id)
                .filter(User::isActive)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .map(UserResponse::fromEntity)
                .toList();
    }

    // 정보 수정
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest req, Long tokenUserId, String tokenRole) {
        if (tokenUserId == null){
            throw new SecurityException("인증 정보가 없습니다.");
        }

        if (!"ADMIN".equals(tokenRole) && !id.equals(tokenUserId)) {
            throw new SecurityException("본인만 수정할수 있습니다.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을수 없습니다."));

        if (!user.isActive()){
            throw new IllegalStateException("비활성화된 사용자입니다.");
        }

        user.setName(req.getName());
        user.setPhone(req.getPhone());

        return UserResponse.fromEntity(user);
    }

    // 유저 비활성화 (소프트 딜리트)
    @Transactional
    public void deactivateUser(Long id,Long tokenUserId, String tokenRole) {
        if (!"ADMIN".equals(tokenRole) && !id.equals(tokenUserId)) {
            throw new SecurityException("본인만 탈퇴할수 있습니다.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setActive(false);
    }



}
