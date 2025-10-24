package service.saju_taro_service.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.service.user.CounselorService;

import java.util.List;

@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorController {
    private final CounselorService counselorService;

    /** ✅ 상담사 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getCounselors(
            @RequestParam(defaultValue = "rating") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(counselorService.getCounselorList(sort,page,size));
    }

    /** ✅ 상담사 상세 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCounselorDetail(@PathVariable Long id) {
        return ResponseEntity.ok(counselorService.getCounselorDetail(id));
    }
}
