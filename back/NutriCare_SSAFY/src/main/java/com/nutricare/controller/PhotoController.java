package com.nutricare.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.model.dto.Photo;
import com.nutricare.model.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/photo-api")
@Tag(name = "Photo RESTful API", description = "사용자 얼굴 사진 CRD을 할수있는 REST API")
public class PhotoController {
	
	private final PhotoService photoService;
	
	public PhotoController(PhotoService photoService) {
		this.photoService = photoService;
	}
	
	@Operation(summary = "사진 메타데이터 단건 조회", description = "photoId로 사진 메타데이터를 조회합니다.")
	@GetMapping("/photo/{photoId}")
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
	@GetMapping("/photo/{userId}")
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
	
	@Operation(summary = "사진 메타데이터 등록", description = "파일 업로드 후 생성된 photoUrl과 userId를 받아 photo 메타데이터를 저장합니다.")
	@PostMapping("/photo")
	public ResponseEntity<?> insert(@RequestBody Photo photo) {
		try {
			int result = photoService.insert(photo);
			if (result > 0) {
				return new ResponseEntity<Integer>(result, HttpStatus.CREATED);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "사진 메타데이터 삭제", description = "photoId로 사진 메타데이터를 삭제합니다.")
	@DeleteMapping("/photo/{photoId}")
	public ResponseEntity<?> delete(@PathVariable("photoId") long photoId) {
		try {
			int result = photoService.delete(photoId);
			if (result > 0) {
				return new ResponseEntity<Void>(HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
