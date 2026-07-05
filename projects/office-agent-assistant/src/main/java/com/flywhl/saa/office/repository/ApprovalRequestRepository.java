package com.flywhl.saa.office.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flywhl.saa.office.model.entity.ApprovalRequest;
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    Optional<ApprovalRequest> findByRequestNo(String requestNo);
}

