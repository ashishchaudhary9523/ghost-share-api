package online.backend.ghostshare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ShareMultipleFiles {
    @Id
    @Column(name = "file_id" , unique = true, nullable = false)
    private String fileId;
    @Column(name = "file_name" , nullable = false)
    private String fileName;
    @Column(name = "file_size")
    private long fileSize;
    @Column(name = "file_type" , nullable = false)
    private String fileType;
    @Lob
    @Column(name = "file_content" , nullable = false)
    private byte[] fileContent;

    private LocalDateTime expiryTime;
    private boolean isExpired = false;
    private int downloads;
}
