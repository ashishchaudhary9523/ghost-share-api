package online.backend.ghostshare.controller;


import online.backend.ghostshare.payload.ShareMultipleFilesDTO;
import online.backend.ghostshare.service.ShareMultipleFilesService;
import online.backend.ghostshare.model.ShareMultipleFiles;
import online.backend.ghostshare.service.ShareOneFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/share-files")
public class ShareMultipleFilesController {

    @Autowired
    private ShareMultipleFilesService shareMultipleFilesService;
    @Autowired
    private ShareOneFileService shareOneFileService;

    @PostMapping("/upload/one-to-one")
    public ResponseEntity<?> uploadOneToOneFile(@RequestParam("file") MultipartFile []file) {
        try {
            if(file.length > 1) {
                String code = shareMultipleFilesService.uploadOneToOne(file);
                return ResponseEntity.ok(code);
            }
            if(file.length == 1){
                ShareMultipleFilesDTO fileDTO = new ShareMultipleFilesDTO();
                fileDTO.setFileName(file[0].getOriginalFilename());
                fileDTO.setFileSize(file[0].getSize());
                fileDTO.setFileType(file[0].getContentType());
                fileDTO.setFileContent(file[0].getBytes());
                String code = shareOneFileService.uploadOneToOne(fileDTO);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/upload/one-to-many")
    public ResponseEntity<?> uploadOneToManyFile(@RequestParam("file") MultipartFile []file) {
        try {
            if(file.length > 1) {
                String code = shareMultipleFilesService.uploadOneToMany(file);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }
            if(file.length == 1){
                ShareMultipleFilesDTO fileDTO = new ShareMultipleFilesDTO();
                fileDTO.setFileName(file[0].getOriginalFilename());
                fileDTO.setFileSize(file[0].getSize());
                fileDTO.setFileType(file[0].getContentType());
                fileDTO.setFileContent(file[0].getBytes());
                String code = shareOneFileService.uploadOneMany(fileDTO);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/get-files/{code}")
    public ResponseEntity<?> getFiles(@PathVariable String code){
        Optional<ShareMultipleFiles> optionalfile = null;
        try {
            optionalfile = shareMultipleFilesService.downloadFile(code);
        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to download file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(optionalfile.isEmpty()){
            return new ResponseEntity<>("File Not Found.", HttpStatus.NOT_FOUND);
        }
        ShareMultipleFiles file = optionalfile.get();
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION ,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.getFileSize())
                    .body(file.getFileContent());
        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to download file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{code}")
    public ResponseEntity<?> deleteFiles(@PathVariable String code){
        try {
            shareMultipleFilesService.deleteFile(code);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
