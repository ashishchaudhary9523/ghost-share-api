package online.backend.ghostshare.cleanupService;


import online.backend.ghostshare.repository.ShareMultipleFilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShareMultipleFilesCleanupService {

    @Autowired
    private ShareMultipleFilesRepository shareMultipleFilesRepository;

    @Scheduled(fixedRate = 5*60*1000)
    public void deleteOldCodes(){
        LocalDateTime current = LocalDateTime.now();
        shareMultipleFilesRepository.deleteExpiredCodes(current);
    }

}
