package com.pacebookcorp.doragee.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
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
import com.pacebookcorp.doragee.entity.Post;
import com.pacebookcorp.doragee.entity.User;
import com.pacebookcorp.doragee.service.FriendService;
import com.pacebookcorp.doragee.service.PostService;
import com.pacebookcorp.doragee.service.UserService;

/**
 * PaceBookController 단위 테스트
 * 
 * @author Kwon Young
 */
@RunWith(MockitoJUnitRunner.class)
public class PaceBookControllerTest {
	@InjectMocks
	private PaceBookController sut;

	@Mock
	private UserService userService;

	@Mock
	private FriendService friendService;

	@Mock
	private PostService postService;

	/**
	 * 1. 모든 사용자 정보 조회
	 */
	@Test
	public void testUsers() {
		List<User> allUser = new ArrayList<>();

		when(userService.findAll()).thenReturn(allUser);

		List<User> resultAllUser = sut.users();

		verify(userService, times(1)).findAll();

		assertNotNull(resultAllUser);
	}

	/**
	 * 2-1. 특정 유저의 뉴스피드 조회 : 정상 case
	 */
	@Test
	public void testNewsfeed() {
		List<Post> newsfeed = new ArrayList<>();

		when(postService.newsfeed("testUserId")).thenReturn(newsfeed);

		List<Post> resultNewsfeed = sut.newsfeed("testUserId");

		verify(postService, times(1)).newsfeed("testUserId");

		assertNotNull(resultNewsfeed);
	}

	/**
	 * 2-2. 특정 유저의 뉴스피드 조회 : 비정상 case (아이디 특수문자 불가)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testNewsfeed_illegalUserId() {
		sut.newsfeed("#doragee");
	}

	/**
	 * 3-1. 특정 유저만의 타임라인(뉴스피드) 조회 : 정상 case
	 */
	@Test
	public void testTimeline() {
		List<Post> newsfeed = new ArrayList<>();

		when(postService.timeline("testUserId")).thenReturn(newsfeed);

		List<Post> resultNewsfeed = sut.timeline("testUserId");

		verify(postService, times(1)).timeline("testUserId");

		assertNotNull(resultNewsfeed);
	}

	/**
	 * 3-2. 특정 유저만의 타임라인(뉴스피드) 조회 : 비정상 case (아이디 5자 이하)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testTimeline_illegalUserId() {
		sut.timeline("tj");
	}

	/**
	 * 4-1. 유저 가입 : 정상 case
	 */
	@Test
	public void testRegist() {
		User user = new User();

		user.setUserId("doragee");
		user.setUserName("도라지의심장");

		when(userService.isInvalidRegistration(user.getUserId(), user.getUserName())).thenReturn(false);
		when(userService.regist(user)).thenReturn(new User());

		User resultUser = sut.regist(user);

		verify(userService, times(1)).regist(user);

		assertNotNull(resultUser);
	}

	/**
	 * 4-2. 유저 가입 : 비정상 case
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRegist_illegalUserId() {
		User user = new User();

		user.setUserId("doragee");
		user.setUserName("도라지의심장");

		when(userService.isInvalidRegistration(user.getUserId(), user.getUserName())).thenReturn(true);

		sut.regist(user);
	}

	/**
	 * 5-1. 친구 신청 : 정상 case
	 */
	@Test
	public void testApply() {
		Friend friend = new Friend();

		when(friendService.isInvalidApply(friend)).thenReturn(false);
		when(friendService.apply(friend)).thenReturn(new Friend());

		Friend resultFriend = sut.apply(friend);

		verify(friendService, times(1)).isInvalidApply(friend);
		verify(friendService, times(1)).apply(friend);

		assertNotNull(resultFriend);
	}

	/**
	 * 5-2. 친구 신청 : 비정상 case
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testApply_illegalFriend() {
		Friend friend = new Friend();

		when(friendService.isInvalidApply(friend)).thenReturn(true);

		sut.apply(friend);
	}

	/**
	 * 6-1. 친구 수락 : 정상 case
	 */
	@Test
	public void testAccept() {
		Friend friend = new Friend();

		when(friendService.notExistNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(false);
		when(friendService.accept(friend)).thenReturn(new Friend());

		Friend resultFriend = sut.accept(friend);

		verify(friendService, times(1)).notExistNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());
		verify(friendService, times(1)).accept(friend);

		assertNotNull(resultFriend);
	}

	/**
	 * 6-2. 친구 수락 : 비정상 case
	 */
	@Test(expected = IllegalStateException.class)
	public void testAccept_illegalFriend() {
		Friend friend = new Friend();

		when(friendService.notExistNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())).thenReturn(true);

		sut.accept(friend);
	}

	/**
	 * 7-1. 친구 끊기 : 정상 case
	 */
	@Test
	public void testEnd() {
		Friend friend = new Friend();

		when(friendService.isInvalidAccepted(friend)).thenReturn(false);
		when(friendService.end(friend)).thenReturn(new Friend());

		Friend resultFriend = sut.end(friend);

		verify(friendService, times(1)).isInvalidAccepted(friend);
		verify(friendService, times(1)).end(friend);

		assertNotNull(resultFriend);
	}

	/**
	 * 7-2. 친구 끊기 : 비정상 case
	 */
	@Test(expected = IllegalStateException.class)
	public void testEnd_illegalFriend() {
		Friend friend = new Friend();

		when(friendService.isInvalidAccepted(friend)).thenReturn(true);

		sut.end(friend);
	}

	/**
	 * 8-1. 팔로우 맺기 : 정상 case
	 */
	@Test
	public void testFollow() {
		Friend friend = new Friend();
		String follower = "applierId";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(false);
		when(friendService.isInvalidUnFollower(friend, follower)).thenReturn(false);
		when(friendService.follow(friend, follower)).thenReturn(new Friend());

		Friend resultFriend = sut.follow(friend, follower);

		verify(friendService, times(1)).isInvalidFollowAgent(follower);
		verify(friendService, times(1)).isInvalidUnFollower(friend, follower);
		verify(friendService, times(1)).follow(friend, follower);

		assertNotNull(resultFriend);
	}

	/**
	 * 8-2. 팔로우 맺기 : 비정상 case / follower 어뷰징
	 */
	@Test(expected = IllegalStateException.class)
	public void testFollow_illegalFollower() {
		Friend friend = new Friend();
		String follower = "abuseFollower";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(true);

		sut.follow(friend, follower);
	}

	/**
	 * 8-3. 팔로우 맺기 : 비정상 case / 친구 관계 아님
	 */
	@Test(expected = IllegalStateException.class)
	public void testFollow_illegalFriend() {
		Friend friend = new Friend();
		String follower = "applierId";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(false);
		when(friendService.isInvalidUnFollower(friend, follower)).thenReturn(true);

		sut.follow(friend, follower);
	}

	/**
	 * 9-1. 팔로우 끊기 : 정상 case
	 */
	@Test
	public void testUnFollow() {
		Friend friend = new Friend();
		String follower = "applierId";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(false);
		when(friendService.isInvalidFollwer(friend, follower)).thenReturn(false);
		when(friendService.unfollow(friend, follower)).thenReturn(new Friend());

		Friend resultFriend = sut.unFollow(friend, follower);

		verify(friendService, times(1)).isInvalidFollowAgent(follower);
		verify(friendService, times(1)).isInvalidFollwer(friend, follower);
		verify(friendService, times(1)).unfollow(friend, follower);

		assertNotNull(resultFriend);
	}

	/**
	 * 9-2. 팔로우 끊기 : 비정상 case / follower 어뷰징
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnFollow_illegalFollower() {
		Friend friend = new Friend();
		String follower = "abuseFollower";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(true);

		sut.unFollow(friend, follower);
	}

	/**
	 * 9-3. 팔로우 끊기 : 비정상 case / 친구 관계 아님
	 */
	@Test(expected = IllegalStateException.class)
	public void testUnFollow_illegalFriend() {
		Friend friend = new Friend();
		String follower = "applierId";

		when(friendService.isInvalidFollowAgent(follower)).thenReturn(false);
		when(friendService.isInvalidFollwer(friend, follower)).thenReturn(true);

		sut.unFollow(friend, follower);
	}

	/**
	 * 10-1. Post 작성 : 정상 case
	 */
	@Test
	public void testWrite() {
		Post post = new Post();

		when(postService.isInvalidWrite(post)).thenReturn(false);
		when(postService.write(post)).thenReturn(new Post());

		Post resultPost = sut.write(post);

		verify(postService, times(1)).isInvalidWrite(post);
		verify(postService, times(1)).write(post);

		assertNotNull(resultPost);
	}

	/**
	 * 10-2. Post 작성 : 비정상 case
	 */
	@Test(expected = IllegalStateException.class)
	public void testWrite_illegalNotAccess() {
		Post post = new Post();

		when(postService.isInvalidWrite(post)).thenReturn(true);

		sut.write(post);
	}

	/**
	 * 11-1. Post 수정 : 정상 case
	 */
	@Test
	public void testModify() {
		Post post = new Post();

		when(postService.isInvalidModify(post)).thenReturn(false);
		when(postService.modify(post)).thenReturn(new Post());

		Post resultPost = sut.modify(post);

		verify(postService, times(1)).isInvalidModify(post);
		verify(postService, times(1)).modify(post);

		assertNotNull(resultPost);
	}

	/**
	 * 11-2. Post 수정 : 비정상 case
	 */
	@Test(expected = IllegalStateException.class)
	public void testModify_illegalNotAccess() {
		Post post = new Post();

		when(postService.isInvalidModify(post)).thenReturn(true);

		sut.modify(post);
	}

	/**
	 * 12-1. Post 삭제 : 정상 case
	 */
	@Test
	public void testDelete() {
		Post post = new Post();

		when(postService.isInvalidDelete(post)).thenReturn(false);
		doNothing().when(postService).delete(post);

		sut.delete(post);

		verify(postService, times(1)).isInvalidDelete(post);
		verify(postService, times(1)).delete(post);
	}

	/**
	 * 12-2. Post 삭제 : 비정상 case
	 */
	@Test(expected = IllegalStateException.class)
	public void testDelete_illegalNotAccess() {
		Post post = new Post();

		when(postService.isInvalidDelete(post)).thenReturn(true);

		sut.delete(post);
	}
}