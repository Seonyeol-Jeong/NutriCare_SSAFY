package com.nutricare.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nutricare.model.dao.HealthProfileDao;
import com.nutricare.model.dao.UserDao;
import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.dto.User;
import com.nutricare.model.dto.UserDetailResponse;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final HealthProfileDao healthProfileDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDao userDao, HealthProfileDao healthProfileDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.healthProfileDao = healthProfileDao;
        this.passwordEncoder = passwordEncoder;
    }

    // 1) 회원가입
    @Override
    public boolean registerUser(User user) {
        String hashed = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashed);

        return userDao.insertUser(user) > 0;
    }

    // 2) 전체조회
    @Override
    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }

    // 3) 상세조회
    @Override
    public User getUserDetail(Long userId) {
        return userDao.findUserById(userId);
    }

    // 4-1) 회원 정보 수정
    @Override
    public boolean updateUserInfo(User user) {
        return userDao.updateUserInfo(user) > 0;
    }
    
    // 4-2) 비밀번호 변경 (핵심 로직)
    @Override
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        // 1. 현재 유저 정보 가져오기 (DB에 저장된 암호화된 비번을 알기 위해)
        User user = userDao.findUserById(userId);
        if (user == null) return false;

        // 2. 현재 비밀번호 검증 (입력받은 평문 vs DB의 해시)
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다."); // 또는 false 반환
        }

        // 3. 새 비밀번호 암호화
        String newHashed = passwordEncoder.encode(newPassword);

        // 4. DB 업데이트
        return userDao.updatePassword(userId, newHashed) > 0;
    }

    // 5) 삭제
    @Override
    public boolean deleteUser(Long userId) {
        return userDao.deleteUser(userId) > 0;
    }

    // 6) 로그인
    @Override
    public User login(String email, String password) {

        // 1) email로 사용자 조회
        User user = userDao.findByEmail(email);
        if (user == null) return null;

        // 2) 입력된 비밀번호(raw) vs DB 해시 비교
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        // 3) 성공시 user 정보 반환
        return user;
    }
    
    // 7) 로그아웃
    @Override
    public void logout(Long userId) {
        // JWT를 사용한다면 서버에서는 특별한 동작 없음 -> 추후 고민할 내용
    }
    
    // 8) 회원 정보 + 건강 정보 조합하여 반환
    @Override
    public UserDetailResponse getUserWithProfile(Long userId) {
        // 1. 유저 기본 정보 조회
        User user = userDao.findUserById(userId);
        
        // 2. 건강 정보 조회 (없으면 null 반환됨)
        HealthProfile hp = healthProfileDao.selectByUserId(userId);
        
        // 3. 두 정보를 DTO 하나로 묶어서 반환
        return new UserDetailResponse(user, hp);
    }

}
