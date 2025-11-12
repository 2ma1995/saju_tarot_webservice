package service.saju_taro_service.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import service.saju_taro_service.domain.role.RoleRequest;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestResponse {
    private Long requestId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userNumber;
    private String requestedRole;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;

    public static RoleRequestResponse fromEntity(RoleRequest r) {
        return RoleRequestResponse.builder()
                .requestId(r.getId())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(r.getUser() != null ? r.getUser().getName() : null)
                .userEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .userNumber(r.getUser() != null? r.getUser().getPhone(): null)
                .requestedRole(r.getRequestedRole() != null ? r.getRequestedRole().name() : null)
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .requestedAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
