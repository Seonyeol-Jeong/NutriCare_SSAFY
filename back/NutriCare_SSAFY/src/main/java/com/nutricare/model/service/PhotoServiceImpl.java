package com.nutricare.model.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.nutricare.model.dao.PhotoDao;
import com.nutricare.model.dto.Photo;

@Service
public class PhotoServiceImpl implements PhotoService {
	
	private final PhotoDao photoDao;
	
	public PhotoServiceImpl(PhotoDao photoDao) {
		this.photoDao = photoDao;
	}
		
	@Override
	public int insert(Photo photo) {
		return photoDao.insert(photo);
	}

	@Override
	@PreAuthorize("@userSecurity.isSelf(#userId, principal)")
	public List<Photo> selectListByUserId(long userId) {
		return photoDao.selectListByUserId(userId);
	}

	@Override
	@PreAuthorize("@dietSecurity.isPhotoOwner(#photoId, principal)")
	public Photo selectOne(long photoId) {
		return photoDao.selectOne(photoId);
	}

	@Override
	@PreAuthorize("@dietSecurity.isPhotoOwner(#photoId, principal)")
	public int delete(long photoId) {
		return photoDao.delete(photoId);
	}

}
