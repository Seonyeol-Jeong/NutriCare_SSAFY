package com.nutricare.model.service;

import java.util.List;

import com.nutricare.model.dto.Photo;

public interface PhotoService {
	int insert(Photo photo);
	List<Photo> selectListByUserId(long userId);
	Photo selectOne(long photoId);
	int delete(long photoId);
}
