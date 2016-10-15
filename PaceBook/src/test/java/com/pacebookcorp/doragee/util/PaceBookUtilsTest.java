package com.pacebookcorp.doragee.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * PaceBookUtils 단위 테스트
 * 
 * @author Kwon Young
 */
public class PaceBookUtilsTest {
	/**
	 * 현재 시간 가져오기
	 */
	@Test
	public void testNowDateTime() {
		PaceBookUtils.nowDateTime();
	}

	/**
	 * 유저 id 등록시 유효성 체크 1 : 정상 case
	 */
	@Test
	public void testIsValidUserId() {
		assertTrue(PaceBookUtils.isValidUserId("doragee"));
		assertTrue(PaceBookUtils.isValidUserId("gosari"));
		assertTrue(PaceBookUtils.isValidUserId("doragee_heart"));
		assertTrue(PaceBookUtils.isValidUserId("abcd5fghi5klmn5abcd5fghi5klmn5abcd5fghi5klmn5")); // 45 자 경계값
	}

	/**
	 * 유저 id 등록시 유효성 체크 2 : 비정상 case
	 */
	@Test
	public void testIsValidUserId_illegal() {
		assertFalse(PaceBookUtils.isValidUserId(""));
		assertFalse(PaceBookUtils.isValidUserId(null));
		assertFalse(PaceBookUtils.isValidUserId("23doragee"));
		assertFalse(PaceBookUtils.isValidUserId("#gosari"));
		assertFalse(PaceBookUtils.isValidUserId("sh"));
		assertFalse(PaceBookUtils.isValidUserId("한글"));
		assertFalse(PaceBookUtils.isValidUserId("abcd5fghi5klmn5abcd5fghi5klmn5abcd5fghi5klmn5a")); // 46 자 경계값
	}

	/**
	 * 유저 이름 등록시 유효성 체크 1 : 정상 case
	 */
	@Test
	public void testIsValidUserName() {
		assertTrue(PaceBookUtils.isValidUserName("도라지"));
		assertTrue(PaceBookUtils.isValidUserName("고사리"));
		assertTrue(PaceBookUtils.isValidUserName("도라지의심장"));
		assertTrue(PaceBookUtils.isValidUserName("DORAJI"));
		assertTrue(PaceBookUtils.isValidUserName("GOSARI86"));
	}

	/**
	 * 유저 이름 등록시 유효성 체크 2 : 비정상 case
	 */
	@Test
	public void testIsValidUserName_illegal() {
		assertFalse(PaceBookUtils.isValidUserName(""));
		assertFalse(PaceBookUtils.isValidUserName(null));
		assertFalse(PaceBookUtils.isValidUserName("#도라지의심장"));
		assertFalse(PaceBookUtils.isValidUserName("abcd5fghi5klmn5abcd5fghi5klmn5abcd5fghi5klmn5a")); // 46 자 경계값
	}

	/**
	 * 아이디 다중 입력 유효성 체크 1: 정상 case
	 */
	@Test
	public void testIsValidUserIds() {
		assertTrue(PaceBookUtils.isValidUserIds("doragee", "gosari", "congnamul"));
	}

	/**
	 * 아이디 다중 입력 유효성 체크 2 : 비정상 case
	 */
	@Test
	public void testIsValidUserIds_illegal() {
		assertFalse(PaceBookUtils.isValidUserIds("doragee", "한글", "congnamul"));
	}

	/**
	 * Friend, Post 테이블에서 사용할 PK 생성
	 */
	@Test
	public void testGeneratePrivateKey() {
		PaceBookUtils.generatePrivateKey("frnd");
	}

	/**
	 * Friend, Post 테이블에서 사용하는 PK 의 유효성 체크, 파라미터에서 PK 어뷰징 방지 가드로직 1 : 정상 case
	 */
	@Test
	public void testIsValidPrivateKey() {
		assertTrue(PaceBookUtils.isValidPrivateKey("frnd20160804171109732", "frnd"));
		assertTrue(PaceBookUtils.isValidPrivateKey("post20160804171109732", "post"));
	}

	/**
	 * Friend, Post 테이블에서 사용하는 PK 의 유효성 체크, 파라미터에서 PK 어뷰징 방지 가드로직 2 : 비정상 case
	 */
	@Test
	public void testIsValidPrivateKey_illegal() {
		assertFalse(PaceBookUtils.isValidPrivateKey("frnd20160frnd71109732", "frnd"));
		assertFalse(PaceBookUtils.isValidPrivateKey("post20160804171109732", "postsdf"));
		assertFalse(PaceBookUtils.isValidPrivateKey("frnd20160frnd71732", "frnd"));
		assertFalse(PaceBookUtils.isValidPrivateKey("post201608041711#09732", "post"));
	}
}