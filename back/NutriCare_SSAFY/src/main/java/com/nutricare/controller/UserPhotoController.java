package com.nutricare.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.nutricare.config.GcsProperties;
import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.AnalysisResult;
import com.nutricare.model.dto.Photo;
import com.nutricare.model.service.AiAnalysisApiService;
import com.nutricare.model.service.AnalysisResultService;
import com.nutricare.model.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user-photos")
@Tag(name = "Photo RESTful API", description = "사용자 얼굴 사진 CRD을 할수있는 REST API")
public class UserPhotoController {

	private final PhotoService photoService;
	private final AiAnalysisApiService aiAnalysisApiService;
	private final AnalysisResultService analysisResultService;
	private final Storage storage; // 파일 업로드를 위해 추가
	private final GcsProperties gcsProps;// 파일 업로드를 위해 추가

	public UserPhotoController(PhotoService photoService, AiAnalysisApiService aiAnalysisApiService,
			AnalysisResultService analysisResultService, Storage storage, GcsProperties gcsProps) {
		this.photoService = photoService;
		this.aiAnalysisApiService = aiAnalysisApiService;
		this.analysisResultService = analysisResultService;
		this.storage = storage;
		this.gcsProps = gcsProps;
	}

	@Operation(summary = "사진 메타데이터 단건 조회", description = "photoId로 사진 메타데이터를 조회합니다.")
	@GetMapping("/{photoId}")
	public ResponseEntity<?> findById(@PathVariable("photoId") long photoId) {
		try {
			Photo photo = photoService.selectOne(photoId);
			if (photo != null) {
				return new ResponseEntity<Photo>(photo, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "사용자별 사진 목록 조회", description = "userId로 업로드된 사진 메타데이터 리스트를 반환합니다.")
	@GetMapping("/users/{userId}")
	public ResponseEntity<?> findListByUserId(@PathVariable("userId") long userId) {
		try {
			List<Photo> photos = photoService.selectListByUserId(userId);
			if (photos != null && !photos.isEmpty()) {
				return new ResponseEntity<List<Photo>>(photos, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "내 사진 목록 조회 (토큰 기반)", description = "로그인한 사용자의 사진 목록을 조회합니다.")
	@GetMapping("/me")
	public ResponseEntity<?> findMyPhotos(@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			Long userId = userDetails.getUser().getUserId();
			List<Photo> photos = photoService.selectListByUserId(userId);
			if (photos != null && !photos.isEmpty()) {
				return new ResponseEntity<List<Photo>>(photos, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
     * [통합된 생성 API]
     * 기능: 파일 업로드 -> DB 저장 -> AI 분석 요청 (One-Stop 처리)
     * 기존 FileController의 로직을 여기로 이동시키고, 기존 insert는 대체합니다.
     */
    @Operation(summary = "사진 등록 및 AI 진단 요청", description = "사진 파일을 업로드하여 저장하고, AI 진단 결과를 생성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // [POST] /api/photos
    public ResponseEntity<?> createPhoto(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUser().getUserId();

            // 1. GCS 업로드 (private 메서드로 분리 권장)
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is required");
            String objectName = buildObjectName(gcsProps.getPrefixPhoto(), String.valueOf(userId), file.getOriginalFilename());
            String fileUrl = uploadToGcs(objectName, file); // FileController에 있던 메서드 가져오기
            Long analysisId = null;
            // 2. Photo 메타데이터 DB 저장
            Photo photo = new Photo(userId, fileUrl);
            photoService.insert(photo);

            // 3. AI 분석 요청 및 결과 저장
            AnalysisResult analysisResult = null;
            String diagnosis = null;
            try {
            	analysisResult = aiAnalysisApiService.requestAnalysis(photo.getPhotoId(), fileUrl);
                if (analysisResult != null) {
                    System.out.println("저장할 데이터 확인: " + analysisResult); 
                    System.out.println("건선 확률(probGunsun): " + analysisResult.getProbGunsun());
                    System.out.println("여드름 확률(probGunsun): " + analysisResult.getProbAcne());
                    System.out.println("아토피 확률(probGunsun): " + analysisResult.getProbAtopy());
                    System.out.println("정상 확률(probGunsun): " + analysisResult.getProbNormal());
                    System.out.println("주사 확률(probGunsun): " + analysisResult.getProbRosacea());
                    System.out.println("지루 확률(probGunsun): " + analysisResult.getProbSeborr());
                    analysisResultService.save(analysisResult);
                    analysisId = analysisResult.getAnalysisId();
                    diagnosis = analysisResult.getDiagnosisName();
                }
            } catch (Exception e) {
                // AI 분석 실패는 로그만 남기고, 사진 등록은 성공으로 처리 (정책에 따라 변경 가능)
                e.printStackTrace(); 
            }

            // 4. 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("analysisId", analysisId);
            response.put("photoId", photo.getPhotoId());
            response.put("fileUrl", fileUrl);
            response.put("diagnosis", diagnosis);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	private String uploadToGcs(String objectName, MultipartFile multipartFile) throws IOException {
		String bucket = gcsProps.getBucketName();
		if (bucket == null || bucket.isBlank()) {
			throw new IllegalStateException("gcs.bucket-name is not set");
		}

		String contentType = multipartFile.getContentType() != null ? multipartFile.getContentType()
				: "application/octet-stream";

		BlobId blobId = BlobId.of(bucket, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

		storage.create(blobInfo, multipartFile.getBytes());

		String baseUrl = (gcsProps.getBaseUrl() != null && !gcsProps.getBaseUrl().isBlank()) ? gcsProps.getBaseUrl()
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
