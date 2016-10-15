package com.pacebookcorp.doragee.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pacebookcorp.doragee.entity.User;
import com.pacebookcorp.doragee.repository.UserRepository;

/**
 * UserService 단위 테스트
 * 
 * @author Kwon Young
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
	@InjectMocks
	private UserService sut;

	@Mock
	private UserRepository userRepository;

	/**
	 * 모든 사용자 정보
	 */
	@Test
	public void testFindAll() {
		List<User> allUser = new ArrayList<>();

		when(userRepository.findAll()).thenReturn(allUser);

		List<User> resultAllUser = sut.findAll();

		verify(userRepository, times(1)).findAll();

		assertNotNull(resultAllUser);
	}

	/**
	 * 유저 가입
	 */
	@Test
	public void testRegistration() {
		User user = new User();

		when(userRepository.save((User) anyObject())).thenReturn(user);

		User resultUser = sut.regist(user);

		verify(userRepository, times(1)).save((User) anyObject());

		assertNotNull(resultUser);
	}

	/**
	 * 유저 가입의 유효성 체크 1 : 정상 case
	 * isValidRegistration() 의 부정 함수인 isInvalidRegistration() 는 단위 TC 없음
	 */
	@Test
	public void testIsValidRegistration() {
		User user = new User();

		user.setUserId("doragee");
		user.setUserName("도라지의심장");

		when(userRepository.findOne("doragee")).thenReturn(null);

		boolean result = sut.isValidRegistration(user.getUserId(), user.getUserName());

		verify(userRepository, times(1)).findOne("doragee");

		assertTrue(result);
	}

	/**
	 * 유저 가입의 유효성 체크 2 : 비정상 case (아이디 특수문자)
	 */
	@Test
	public void testIsValidRegistration_illegalUserId() {
		User user = new User();

		user.setUserId("#doragee");
		user.setUserName("도라지의심장");

		boolean result = sut.isValidRegistration(user.getUserId(), user.getUserName());

		verify(userRepository, times(0)).findOne("#doragee");

		assertFalse(result);
	}

	/**
	 * 유저 가입의 유효성 체크 3 : 비정상 case (아이디 중복)
	 */
	@Test
	public void testIsValidRegistration_duplicationId() {
		User user = new User();

		user.setUserId("doragee");
		user.setUserName("도라지의심장");

		when(userRepository.findOne("doragee")).thenReturn(new User());

		boolean result = sut.isValidRegistration(user.getUserId(), user.getUserName());

		verify(userRepository, times(1)).findOne("doragee");

		assertFalse(result);
	}
}