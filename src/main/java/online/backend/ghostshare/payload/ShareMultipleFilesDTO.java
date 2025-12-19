package online.backend.ghostshare.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareMultipleFilesDTO {
    private String fileId;
    @NotEmpty
    private String fileName;
    @NotEmpty
    private long fileSize;
    @NotEmpty
    private String fileType;
    @NotEmpty
    private byte[] fileContent;

}
