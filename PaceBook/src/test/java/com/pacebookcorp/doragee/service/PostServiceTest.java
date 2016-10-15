package com.pacebookcorp.doragee.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
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
import com.pacebookcorp.doragee.repository.FriendRepository;
import com.pacebookcorp.doragee.repository.PostRepository;

/**
 * PostService 의 단위 테스트
 * 
 * @author Kwon Young
 */
@RunWith(MockitoJUnitRunner.class)
public class PostServiceTest {
	@InjectMocks
	private PostService sut;

	@Mock
	private PostRepository postRepository;

	@Mock
	private FriendRepository friendRepository;

	/**
	 * 특정 유저의 뉴스피드 조회
	 */
	@Test
	public void testGetTotalNewsfeedBy() {
		List<Post> post = new ArrayList<>();

		when(postRepository.newsfeed("doragee")).thenReturn(post);

		List<Post> resultPost = sut.newsfeed("doragee");

		verify(postRepository, times(1)).newsfeed("doragee");

		assertNotNull(resultPost);
	}

	/**
	 * 특정 유저만의 타임라인(뉴스피드) 조회
	 */
	@Test
	public void testGetMyNewsFeedBy() {
		List<Post> post = new ArrayList<>();

		when(postRepository.timeline("doragee")).thenReturn(post);

		List<Post> resultPost = sut.timeline("doragee");

		verify(postRepository, times(1)).timeline("doragee");

		assertNotNull(resultPost);
	}

	/**
	 * Post 작성의 유효성 체크 1: 정상 case / 자신에게 쓴글
	 * isValidWritePost() 의 부정 함수인 isInvalidWrite() 는 단위 TC 없음
	 */
	@Test
	public void testIsValidWritePost_writeMe() {
		Post post = new Post();

		post.setOwnerId("doragee");
		post.setCreatorId("doragee");
		post.setContent("내가 내 Post에 글을 쓴다");

		boolean result = sut.isValidWrite(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertTrue(result);
	}

	/**
	 * Post 작성의 유효성 체크 2: 정상 case / 친구에게 쓴글
	 */
	@Test
	public void testIsValidWritePost_writeFriend() {
		Post post = new Post();

		post.setOwnerId("doragee");
		post.setCreatorId("gosari");
		post.setContent("gosari 가 doragee 에게 글을 쓴다");

		// 친구관계라면 Frined 객체 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId())).thenReturn(new Friend());

		boolean result = sut.isValidWrite(post);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertTrue(result);
	}

	/**
	 * Post 작성의 유효성 체크 3: 비정상 case / 친구 아닌 사람에게 글 작성 시도
	 */
	@Test
	public void testIsValidWritePost_write_butNotFriend() {
		Post post = new Post();

		post.setOwnerId("doragee");
		post.setCreatorId("gosari");
		post.setContent("gosari 가 친구가 아닌 doragee 에게 글을 쓴다");

		// 친구가 아니라면 null 을 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId())).thenReturn(null);

		boolean result = sut.isValidWrite(post);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertFalse(result);
	}

	/**
	 * Post 작성의 유효성 체크 4: 비정상 case / 유효하지 않은 계정(특수문자 포함 or 5자 이하)
	 */
	@Test
	public void testIsValidWritePost_invalidUserId() {
		Post post = new Post();

		post.setOwnerId("#doragee");
		post.setCreatorId("dd");
		post.setContent("유효하지 않은 계정끼리 글을 쓴다.");

		boolean result = sut.isValidWrite(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertFalse(result);
	}

	/**
	 * Post 작성
	 */
	@Test
	public void testWrite() {
		Post post = new Post();

		when(postRepository.save((Post) anyObject())).thenReturn(post);

		Post resultPost = sut.write(post);

		verify(postRepository, times(1)).save((Post) anyObject());

		assertNotNull(resultPost);
	}

	/**
	 * isValidModify 의 부정 함수인 isInvalidModify 는 단위 TC 없음 
	 * Post 수정의 유효성 체크 1 : 정상 case / 자신이 자신에게 쓴 글을 수정
	 */
	@Test
	public void testIsValidModify_modify_myPost() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setCreatorId("doragee");
		post.setModifierId("doragee");
		post.setContent("내가 내게 쓴 글을 수정한다.");

		boolean result = sut.isValidModify(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertTrue(result);
	}

	/**
	 * Post 수정의 유효성 체크 2 : 정상 case / 자신이 자신에게 쓴 글을 수정
	 */
	@Test
	public void testIsValidModifyPost_modify_myFriend() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setCreatorId("gosari");
		post.setModifierId("gosari");
		post.setContent("gosari 가 doragee 에게 남겼었던 글을 수정한다.");

		// 친구 관계라면 Friend 객체가 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId())).thenReturn(new Friend()); 

		boolean result = sut.isValidModify(post);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId());

		assertTrue(result);
	}

	/**
	 * Post 수정의 유효성 체크 3 : 비정상 case / 다른 사람의 글을 수정시도 하나 친구가 아님
	 */
	@Test
	public void testIsValidModifyPost_tryModify_other() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setCreatorId("doragee");
		post.setModifierId("gosari");
		post.setContent("gosari 가 친구가 아닌 doragee 글을 수정 시도한다.");

		// 친구 관계가 아니라면 null 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId())).thenReturn(null);

		boolean result = sut.isValidModify(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId());

		assertFalse(result);
	}

	/**
	 * Post 수정의 유효성 체크 4 : 비정상 case / PK 어뷰징
	 */
	@Test
	public void testIsValidModifyPost_tryAbuse_illegalPkValue() {
		Post post = new Post();

		post.setPostPk("post20160englist09732");
		post.setOwnerId("doragee");
		post.setModifierId("gosari");
		post.setContent("PK 의 Prefix 뒤에 숫자가 아닌 영문자가 있음.");

		boolean result = sut.isValidModify(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertFalse(result);
	}

	/**
	 * Post 수정
	 */
	@Test
	public void testModify() {
		Post post = new Post();
		Post modifiedPost = new Post();

		post.setPostPk("testPk");
		modifiedPost.setContent("글을 수정합니다");
		modifiedPost.setModifierId("doragee");

		when(postRepository.findOne(post.getPostPk())).thenReturn(modifiedPost);
		when(postRepository.save(modifiedPost)).thenReturn(new Post());

		Post resultPost = sut.modify(post);

		verify(postRepository, times(1)).findOne(post.getPostPk());
		verify(postRepository, times(1)).save(modifiedPost);

		assertNotNull(resultPost);
	}

	/**
	 * Post 를 삭제하기 전의 유효성 체크 1 : 정상 case / 내가 내 Post 에 작성한 글을 삭제
	 * isValidDelete 의 부정 함수인 isInvalidDelete 는 단위 TC 없음 
	 */
	@Test
	public void testIsValidDelete() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setModifierId("doragee");

		boolean result = sut.isValidDelete(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getCreatorId());

		assertTrue(result);
	}

	/**
	 * Post 를 삭제하기 전의 유효성 체크 2 : 정상 case / 내가 내 친구에게 작성한 Post 를 삭제
	 */
	@Test
	public void testIsValidDelete_myFriend() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setModifierId("gosari");

		// 친구 관계라면 Friend 객체를 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId())).thenReturn(new Friend()); 

		boolean result = sut.isValidDelete(post);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId());

		assertTrue(result);
	}

	/**
	 * Post 를 삭제하기 전의 유효성 체크 3 : 비정상 case / 친구가 아닌 사람의 글을 삭제시도
	 */
	@Test
	public void testIsValidDelete_tryDelete_other() {
		Post post = new Post();

		post.setPostPk("post20160804171109732");
		post.setOwnerId("doragee");
		post.setModifierId("gosari");

		// 친구 관계가 아니라면 null 리턴
		when(friendRepository.findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId())).thenReturn(null);

		boolean result = sut.isValidDelete(post);

		verify(friendRepository, times(1)).findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId());

		assertFalse(result);
	}

	/**
	 * Post 를 삭제하기 전의 유효성 체크 4 : 비정상 case / PK 값 어뷰징
	 */
	@Test
	public void testIsValidDelete_tryAbuse_illegalPkValue() {
		Post post = new Post();

		post.setPostPk("20160post804171109732");
		post.setOwnerId("doragee");
		post.setModifierId("gosari");
		post.setContent("PK가 prefix('post')가 상이한 곳에 있음");

		boolean result = sut.isValidDelete(post);

		verify(friendRepository, times(0)).findAcceptedFriendRelation(post.getOwnerId(), post.getModifierId());

		assertFalse(result);
	}

	/**
	 * Post 삭제
	 */
	@Test
	public void testDelete() {
		Post post = new Post();
		post.setPostPk("testPk");

		when(postRepository.findOne(post.getPostPk())).thenReturn(new Post());
		doNothing().when(postRepository).delete(post);

		sut.delete(post);

		verify(postRepository, times(1)).findOne(post.getPostPk());
	}
}