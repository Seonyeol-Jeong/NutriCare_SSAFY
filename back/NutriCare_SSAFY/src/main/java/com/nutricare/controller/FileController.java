package com.nutricare.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.nutricare.config.GcsProperties;
import com.nutricare.model.dto.Board;
import com.nutricare.model.dto.BoardImage;
import com.nutricare.model.dto.Photo;
import com.nutricare.model.service.BoardService;
import com.nutricare.model.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/file-api")
@Tag(name = "File Upload API", description = "Google Cloud Storage 업로드 API")
public class FileController {

    private final PhotoService photoService;
    private final BoardService boardService;
    private final Storage storage;
    private final GcsProperties gcsProps;

    public FileController(PhotoService photoService,
                          BoardService boardService,
                          Storage storage,
                          GcsProperties gcsProps) {
        this.photoService = photoService;
        this.boardService = boardService;
        this.storage = storage;
        this.gcsProps = gcsProps;
    }

    @Operation(summary = "게시글 이미지 업로드(GCS)", description = "GCS에 업로드 후 board_image에 저장")
    @PostMapping("/upload-board-image")
    public ResponseEntity<?> uploadBoardImage(@RequestParam("boardId") Long boardId,
                                              @RequestParam("file") List<MultipartFile> files) {
        try {
            if (boardId == null || files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("boardId and files are required");
            }
            List<BoardImage> images = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            for (MultipartFile f : files) {
                if (f.isEmpty()) continue;
                String objectName = buildObjectName(gcsProps.getPrefixBoard(), String.valueOf(boardId), f.getOriginalFilename());
                String fileUrl = uploadToGcs(objectName, f);
                images.add(new BoardImage(boardId, fileUrl));
                urls.add(fileUrl);
            }

            if (!images.isEmpty()) {
                Board board = new Board();
                board.setBoardId(boardId);
                board.setImages(images);
                boardService.insertBoardImages(board);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("boardId", boardId, "imageUrls", urls));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File Upload Failed");
        }
    }

    @Operation(summary = "Photo 업로드(GCS) 및 메타 저장", description = "JWT에서 userId를 읽어 GCS 업로드 후 photo 테이블에 저장")
    @PostMapping("/upload-with-meta")
    public ResponseEntity<?> uploadAndSavePhoto(@RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return new ResponseEntity<>("Invalid token: userId not found", HttpStatus.UNAUTHORIZED);
            }
            if (file.isEmpty()) {
                return new ResponseEntity<>("file is required", HttpStatus.BAD_REQUEST);
            }

            String objectName = buildObjectName(gcsProps.getPrefixPhoto(), String.valueOf(userId), file.getOriginalFilename());
            String fileUrl = uploadToGcs(objectName, file);

            Photo photo = new Photo(userId, fileUrl);
            photoService.insert(photo);

            Map<String, Object> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("userId", userId);
            response.put("photoId", photo.getPhotoId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("File Upload Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String uploadToGcs(String objectName, MultipartFile multipartFile) throws IOException {
        String bucket = gcsProps.getBucketName();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("gcs.bucket-name is not set");
        }

        String contentType = multipartFile.getContentType() != null
                ? multipartFile.getContentType()
                : "application/octet-stream";

        BlobId blobId = BlobId.of(bucket, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, multipartFile.getBytes());

        String baseUrl = (gcsProps.getBaseUrl() != null && !gcsProps.getBaseUrl().isBlank())
                ? gcsProps.getBaseUrl()
                : "https://storage.googleapis.com";

        return baseUrl + "/" + bucket + "/" + objectName;
    }

    private String buildObjectName(String prefix, String ownerId, String originalFilename) {
        String cleanPrefix = (prefix != null) ? prefix.trim() : "";
        if (!cleanPrefix.isEmpty() && !cleanPrefix.endsWith("/")) {
            cleanPrefix = cleanPrefix + "/";
        }
        String ownerSegment = (ownerId != null && !ownerId.isBlank()) ? ownerId + "/" : "";
        String filename = UUID.randomUUID() + "_" + originalFilename;
        return cleanPrefix + ownerSegment + filename;
    }
}
