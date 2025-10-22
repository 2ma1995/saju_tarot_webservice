package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.consultation.Consultation;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
}
