package com.pacebookcorp.doragee.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pacebookcorp.doragee.entity.User;
import com.pacebookcorp.doragee.repository.UserRepository;
import com.pacebookcorp.doragee.util.PaceBookUtils;

/**
 * 전체 유저 목록과 가입 기능을 담당하는 클래스
 * 
 * @author Kwon Young
 */
@Service
public class UserService {	
	@Autowired
	private UserRepository userRepository;

	/**
	 * 1. 모든 사용자 정보 조회
	 * 
	 * @return	서비스에 등록된 모든 사용자 정보
	 */
	public List<User> findAll() {
		return userRepository.findAll();
	}

	/**
	 * 4. 유저 가입
	 * 
	 * @param 	user	: userId, userName 만 입력받는다.
	 * @return	유저 가입은 계정과 이름만 받는다. 단, 유저 계정은 영문/숫자 5~45자 이내, 유저 이름은 영문/숫자/한글 1~45자 이내이며 유저 계정은 기존 User table 에 있는지 중복체크를 한다.
	 */
	public User regist(User user) {		
		return userRepository.save(createUser(user));
	}

	/**
	 * User table 에 입력할 User 클래스 생성
	 * 
	 * @param	user	: userId, userName 만 입력받는다.
	 * @return	가입 시도한 유저 정보
	 */
	private User createUser(User user) {
		String userId = user.getUserId();
		String userName = user.getUserName();
		Date now = PaceBookUtils.nowDateTime();

		return new User(userId, userName, userId, now, userId, now);
	}

	/**
	 * 유저 가입의 유효성 체크
	 * 
	 * @param 	user 의 id 와 이름만 입력받는다.
	 * @return	id 와 이름이 입력 표현이 맞거나, id 가 기존에 등록된 계정이 아니라면 true 를 리턴한다.
	 */
	public boolean isValidRegistration(String userId, String userName) {		
		if (PaceBookUtils.isInvalidUserId(userId)) {
			return false;
		}

		if (PaceBookUtils.isInvalidUserName(userName)) {
			return false;
		}

		if (Objects.isNull(userRepository.findOne(userId)) == false) {
			return false;
		}

		return true;
	}

	/**
	 * isValidRegistration 의 부정 함수
	 * 
	 * @param 	user 의 id 와 이름만 입력받는다.
	 * @return	id 와 이름이 입력 표현이 틀리거나, id 가 기존에 등록된 계정이라면 true 를 리턴한다.
	 */
	public boolean isInvalidRegistration(String userId, String userName) {
		return isValidRegistration(userId, userName) == false;
	}
}