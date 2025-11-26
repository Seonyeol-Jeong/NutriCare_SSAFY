package com.nutricare.model.dao;

import com.nutricare.model.dto.Photo;

public interface PhotoDao {
	
	int insert(Photo photo);
	boolean selectListByUserId(long userId);
	boolean selectOne(long photoId);
	int delete(long photoId);
}
