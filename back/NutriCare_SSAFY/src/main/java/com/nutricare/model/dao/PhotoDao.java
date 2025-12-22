package com.nutricare.model.dao;

import java.util.List;

import com.nutricare.model.dto.Photo;

public interface PhotoDao {
	
	int insert(Photo photo);
	List<Photo> selectListByUserId(long userId);
	Photo selectOne(long photoId);
	int delete(long photoId);
}
