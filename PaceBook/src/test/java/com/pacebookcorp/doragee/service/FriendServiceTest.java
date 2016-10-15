package com.pacebookcorp.doragee.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

import com.pacebookcorp.doragee.entity.Friend;
import com.pacebookcorp.doragee.repository.FriendRepository;
import com.pacebookcorp.doragee.repository.UserRepository;

/**
 * FriendService 의 단위 테스트
 * 
 * @author Kwon Young
 */
@RunWith(MockitoJUnitRunner.class)
public class FriendServiceTest {
	@InjectMocks
	private FriendService sut;

	@Mock
	private UserRepository userRepository;

	@Mock
	private FriendRepository friendRepository;

	/**
	 * 친구를 신청하기전 신청자, 수락자의 유효성 체크 1 : 정상 case 
	 * isValidApply 의 부정 함수인 isInvalidApply 는 단위 TC 없음
	 */
	@Test
	public void testIsValidApply() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		List<String> userIds = new ArrayList<>();
		userIds.add("doragee");
		userIds.add("gosari");

		when(userRepository.findUsers(friend.getApplierId(), friend.getAcceptorId())).thenReturn(userIds);
		// 친구 신청 이력이 없어야 하기때문에 null 리턴
		when(friendRepository.existFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(null);

		boolean result = sut.isValidApply(friend);

		verify(userRepository, times(1)).findUsers(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).existFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertTrue(result);
	}

	/**
	 * 친구를 신청하기전 신청자, 수락자의 유효성 체크 2 : 비정상 case / 유저 한명이 계정 삭제로 친구 신청 불가능
	 */
	@Test
	public void testIsValidApply_notExistUser() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		List<String> userIds = new ArrayList<>();
		userIds.add("doragee");
		// userIds.add("gosari"); // 계정 삭제

		when(userRepository.findUsers(friend.getApplierId(), friend.getAcceptorId())).thenReturn(userIds);

		boolean result = sut.isValidApply(friend);

		verify(userRepository, times(1)).findUsers(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(0)).findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertFalse(result);
	}

	/**
	 * 친구를 신청하기전 신청자, 수락자의 유효성 체크 3 : 비정상 case / 이미 신청 이력이 있음
	 */
	@Test
	public void testIsValidApply_existFriendRelation() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		List<String> userIds = new ArrayList<>();
		userIds.add("doragee");
		userIds.add("gosari");

		when(userRepository.findUsers(friend.getApplierId(), friend.getAcceptorId())).thenReturn(userIds);
		// 친구 신청 이력이 있으면 Friend 객체 리턴
		when(friendRepository.existFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(new Friend());

		boolean result = sut.isValidApply(friend);

		verify(userRepository, times(1)).findUsers(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).existFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertFalse(result);
	}

	/**
	 * 친구 신청
	 */
	@Test
	public void testApply() {
		Friend friend = new Friend();

		when(friendRepository.save(friend)).thenReturn(new Friend());

		Friend resultFriend = sut.apply(friend);

		verify(friendRepository, times(1)).save(friend);

		assertNotNull(resultFriend);
	}

	/**
	 * 친구 신청을 수락하기 전 유효성 체크 1 : 정상 case 
	 * existNotAccptedFriendRelation 의 부정 함수인 notExistNotAccptedFriendRelation 는 단위 TC 없음
	 */
	@Test
	public void testExistNotAccptedFriendRelation() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		// 친구 신청 이력이 있다면 Friend 객체를 리턴
		when(friendRepository.findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(new Friend());

		boolean result = sut.existNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		verify(friendRepository, times(1)).findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertTrue(result);
	}

	/**
	 * 친구 신청을 수락하기 전 유효성 체크 2 : 비정상 case / 친구 신청 이력이 있어야 하나 (이력이) 없음
	 */
	@Test
	public void testExistNotAccptedFriendRelation_noneAccept() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		// 친구 신청 이력이 없다면 null 리턴
		when(friendRepository.findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(null);

		boolean result = sut.existNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		verify(friendRepository, times(1)).findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertFalse(result);
	}

	/**
	 * 친구 수락
	 */
	@Test
	public void testAccept() {
		Friend friend = new Friend();
		Friend existFriend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		when(friendRepository.findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(existFriend);
		when(friendRepository.save(existFriend)).thenReturn(new Friend());

		Friend resultFriend = sut.accept(friend);

		verify(friendRepository, times(1)).findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).save(existFriend);

		assertNotNull(resultFriend);
	}

	/**
	 * 친구 관계를 끊거나 언팔, 팔로우 하기 위해선 먼저 '친구' 였는지부터 검사하는 메서드 1 : 정상 case
	 * isValidAccepted 의 부정 함수인 isInvalidAccepted 는 단위 TC 없음
	 */
	@Test
	public void testIsValidAccepted() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		// 친구 관계라면 Friend 객체를 리턴
		when(friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(new Friend());

		boolean result = sut.isValidAccepted(friend);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertTrue(result);
	}

	/**
	 * 친구 관계를 끊거나 언팔, 팔로우 하기 위해선 먼저 '친구' 였는지부터 검사하는 메서드 2 : 비정상 case
	 */
	@Test
	public void testIsValidAccepted_notFriendEachOther() {
		Friend friend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		// 친구 관계가 아니라면 null
		when(friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(null);

		boolean result = sut.isValidAccepted(friend);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		assertFalse(result);
	}

	/**
	 * Follower 의 주체가 신청자인지 수락자인지 명확해야 한다. 파라메터 어뷰징 방지 가드로직 1 : 정상 case
	 */
	@Test
	public void testIsValidFollowAgent() {
		assertTrue(sut.isValidFollowAgent("applierId"));
		assertTrue(sut.isValidFollowAgent("acceptorId"));
	}

	/**
	 * Follower 의 주체가 신청자인지 수락자인지 명확해야 한다. 파라메터 어뷰징 방지 가드로직 2 : 비정상 case / applierId, acceptorId가 아님
	 */
	@Test
	public void testIsValidFollowAgent_tryAbuse() {
		assertFalse(sut.isValidFollowAgent("abuseApplier"));
		assertFalse(sut.isValidFollowAgent("abuseAcceptor"));
	}

	/**
	 * 친구 끊기
	 */
	@Test
	public void testEnd() {
		Friend friend = new Friend();
		Friend existFriend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		when(friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(existFriend);
		when(friendRepository.save(existFriend)).thenReturn(new Friend());

		Friend resultFriend = sut.end(friend);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).save(existFriend);

		assertNotNull(resultFriend);
	}

	/**
	 * 친구 관계에 있는 사람 팔로우 맺기
	 */
	@Test
	public void testFollow() {
		Friend friend = new Friend();
		Friend existFriend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		when(friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(existFriend);
		when(friendRepository.save(existFriend)).thenReturn(new Friend());

		Friend resultFriend = sut.follow(friend, "applierId");

		verify(friendRepository, times(1)).findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).save(existFriend);

		assertNotNull(resultFriend);
	}

	/**
	 * 친구 관계에 있는 사람 팔로우 끊기
	 */
	@Test
	public void testUnfollow() {
		Friend friend = new Friend();
		Friend existFriend = new Friend();

		friend.setApplierId("doragee");
		friend.setAcceptorId("gosari");

		when(friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(existFriend);
		when(friendRepository.save(existFriend)).thenReturn(new Friend());

		Friend resultFriend = sut.follow(friend, "acceptorId");

		verify(friendRepository, times(1)).findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());
		verify(friendRepository, times(1)).save(existFriend);

		assertNotNull(resultFriend);
	}
}