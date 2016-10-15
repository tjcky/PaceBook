package com.pacebookcorp.doragee.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Service 단에서 공통으로 쓰이는 유틸성 함수를 분리해둔 클래스
 * 
 * @author Kwon Young
 */
public class PaceBookUtils {
	public static final String NO = "n";
	public static final String YES = "y";

	private static final String USER_ID = "^[a-zA-Z]{1}[a-zA-Z0-9_]{5,44}$";
	private static final String USER_NAME = "^[a-zA-Z0-9ㄱ-ㅎ가-힣]{1,44}$";
	private static final String AFTER_PREFIX = "^[0-9]{17}$";
	private static final int INVALID_USERID_COUNT_IS_ZERO = 0;
	private static final int MAX_SIZE = 45;

	/**
	 * 현재 시간 가져오기 
	 * Example : Sat Aug 06 17:49:03 KST 2016
	 */
	public static Date nowDateTime() {
		return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 유저 id 등록시 유효성 체크 
	 * Example : tjc(불가, 5자 이하), #doragee(불가, 특수문자), 342doragee(불가, 숫자 시작), doragee(가능)
	 * 
	 * @param userId : 입력받은 유저 계정
	 * @return 유저 아이디는 영문/숫자, 시작은 영문자로만, "_"를 제외한 특수문자 불가, 5~45자 이하가 충족되면 true 를리턴한다.
	 */
	public static boolean isValidUserId(String userId) {
		if (StringUtils.isEmpty(userId)) {
  			return false;
  		}

		return Pattern.matches(USER_ID, userId);
	}

	/**
	 * isValidUserId 의 부정 함수 
	 * Example : tjc(불가, 5자 이하), #doragee(불가, 특수문자), 342doragee(불가, 숫자 시작), doragee(가능)
	 * 
	 * @param userId : 입력받은 유저 계정
	 * @return 유저 아이디는 영문/숫자, 시작은 영문자로만, "_"를 제외한 특수문자 불가, 5~45 범위가 충족되지 않는다면 true 를 리턴한다.
	 */
	public static boolean isInvalidUserId(String userId) {
		return isValidUserId(userId) == false;
	}

	/**
	 * 유저 이름 등록시 유효성 체크 
	 * Example : #DORAGE(불가, 특수문자), 도라지(가능)
	 * 유저 이름에 한글이 포함된 경우 DB 컬럼(t_user.crer_nm)의 최대 사이즈 이내에 지정되어야 하므로 byte 길이 체크가 필요하다. 
	 * 
	 * @param userName : 입력받은 유저 이름
	 * @return 유저 이름은 영문/숫자/한글, 1~45자 범위라면 true 를 리턴한다.
	 */
	public static boolean isValidUserName(String userName) {
		if (StringUtils.isEmpty(userName)) {
  			return false;
  		}

		if (userName.getBytes().length > MAX_SIZE) {
			return false;
		}

		return Pattern.matches(USER_NAME, userName);
	}

	/**
	 * isValidUserName 의 부정 함수
	 * 
	 * @param userName : 입력받은 유저 이름
	 * @return 유저 이름은 영문/숫자/한글, 1~45자 범위가 충족되지 않는다면 true 를 리턴한다.
	 */
	public static boolean isInvalidUserName(String userName) {
		return isValidUserName(userName) == false;
	}

	/**
	 * 아이디 다중 입력 유효성 체크
	 * 
	 * @param userIds ('tjcky', 'doragee', 'gosari', ....)
	 * @return
	 */
	public static boolean isValidUserIds(String... userIds) {
		Stream<String> userIdStream = Stream.of(userIds);

		return userIdStream.filter(w -> PaceBookUtils.isInvalidUserId(w)).count() == INVALID_USERID_COUNT_IS_ZERO;
	}

	/**
	 * isValidUserIds 의 부정 함수
	 * 
	 * @param 설명 생략
	 */
	public static boolean isInvalidUserIds(String... userIds) {
		return isValidUserIds(userIds) == false;
	}

	/**
	 * Friend, Post 테이블에서 사용할 PK 생성 
	 * Example : 'frnd' 를 입력받으면 frnd20160101011122333 을 리턴
	 * 
	 * @param PK의 prefix
	 */
	public static String generatePrivateKey(String prefix) {
		Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		String nowDateTime = formatter.format(now);

		return prefix + nowDateTime;
	}

	/**
	 * Friend, Post 테이블에서 사용하는 PK 의 유효성 체크, 파라미터에서 PK 어뷰징 방지 가드로직 
	 * Example : frnd20160804171109732 -> frnd + 숫자 17자리 정상 PK
	 * 
	 * @param privateKey 		PK 값
	 * @param privateKeyPrefix 	PK Prefix
	 * @return PK 가 올바른 형태라면 true 를 리턴한다.
	 */
	public static boolean isValidPrivateKey(String privateKey, String privateKeyPrefix) {
		if (StringUtils.startsWith(privateKey, privateKeyPrefix) == false) {
			return false;
		}

		String afterPrefix = StringUtils.substringAfter(privateKey, privateKeyPrefix);

		return Pattern.matches(AFTER_PREFIX, afterPrefix);
	}

	/**
	 * Friend, Post 테이블에서 사용하는 PK 의 유효성 체크, 파라미터에서 PK 어뷰징 방지 가드로직 
	 * Example : frnd201608041711097321 -> frnd + 숫자 17자리 정상 PK
	 * 
	 * @param privateKey		PK 값
	 * @param privateKeyPrefix	PK Prefix
	 * @return PK 가 올바른 형태가 아니라면 true 를 리턴한다.
	 */
	public static boolean isInvalidPrivateKey(String privateKey, String privateKeyPrefix) {
		return isValidPrivateKey(privateKey, privateKeyPrefix) == false;
	}
}