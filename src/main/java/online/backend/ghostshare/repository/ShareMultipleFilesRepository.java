package online.backend.ghostshare.repository;


import jakarta.transaction.Transactional;
import online.backend.ghostshare.model.ShareMultipleFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ShareMultipleFilesRepository extends JpaRepository<ShareMultipleFiles, String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ShareMultipleFiles f WHERE f.expiryTime <= :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
}
