package com.pacebookcorp.doragee.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pacebookcorp.doragee.entity.Friend;
import com.pacebookcorp.doragee.entity.Post;
import com.pacebookcorp.doragee.entity.User;
import com.pacebookcorp.doragee.exception.ExceptionMessage;
import com.pacebookcorp.doragee.service.FriendService;
import com.pacebookcorp.doragee.service.PostService;
import com.pacebookcorp.doragee.service.UserService;
import com.pacebookcorp.doragee.util.PaceBookUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Kwon Young
 */
@RestController
@Slf4j
public class PaceBookController {
	private static final String INVALID_FRIEND_MESSAGE = "In Friend Relation, acceptorId or applierId is not Friend.";
	private static final String INVALID_FRIEND_AND_ABUSE_MESSAGE = "In Friend Relation, acceptorId or applierId is not Friend, or follwer is not acceptable.";
	private static final String USERID_IS_ILLEGAL = "userId is illegal.";
	private static final String NOT_FRIEND_EACH_OTHER = "In Friend Relation, acceptorId or applierId is not acceptable each other.";
	private static final String USER_ID_NAME_IS_ILLEGAL = "userId or userName is illegal.";
	private static final String APPLIER_OR_ACCEPTOR_ILLEGAL_OR_EXIST_FRIEND = "applierId or acceptorId is illegal or Exist friend each other.";
	private static final String SERVER_ERROR = "Server Error";

	@Autowired
	private UserService userService;

	@Autowired
	private FriendService friendService;

	@Autowired
	private PostService postService;

	/**
	 * 1. 모든 사용자 정보 조회
	 * 
	 * @return	서비스에 등록된 모든 사용자 정보
	 */
	@RequestMapping(value = "/v1/users", method = RequestMethod.GET)
	public List<User> users() {
		return userService.findAll();
	}

	/**
	 * 2. 특정 유저의 뉴스피드 조회
	 * 
	 * @param 	userId 특정 유저 계정(id)
	 * @return	userId 와 친구관계를 맺은 사람들이 작성한 Post 목록을 가져오는데, 친구 관계더라도 팔로우가 활성화 된 친구의 Post만 가져온다. 정렬 기준은 Post 가 수정된 날짜 기준이다.(수정일이 작성일보다 최근)
	 */
	@RequestMapping(value = "/v1/newsfeed/{userId}", method = RequestMethod.GET)
	public List<Post> newsfeed(@PathVariable String userId) {
		if (PaceBookUtils.isInvalidUserId(userId)) {
			log.info(USERID_IS_ILLEGAL + " : {}", userId);
			throw new IllegalArgumentException(USERID_IS_ILLEGAL);
		}

		return postService.newsfeed(userId);
	}

	/**
	 * 3. 특정 유저의 타임라인 조회
	 * 
	 * @param	userId 특정 유저 계정(id)
	 * @return	userId 의 타임라인에 게시된 Post를 가져오는데, 자신이 작성한 것과 친구가 자신에게 남긴 글을 가져온다. 
	 */
	@RequestMapping(value = "/v1/timeline/{userId}", method = RequestMethod.GET)
	public List<Post> timeline(@PathVariable String userId) {
		if (PaceBookUtils.isInvalidUserId(userId)) {
			log.info(USERID_IS_ILLEGAL + " : {}", userId);
			throw new IllegalArgumentException(USERID_IS_ILLEGAL);
		}

		return postService.timeline(userId);
	}

	/**
	 * 4. 유저 가입
	 * 
	 * @param 	user	: userId, userName 만 입력받는다.
	 * @return	유저 가입은 계정과 이름만 받는다. 단, 유저 계정은 영문/숫자 5~45자 이내, 유저 이름은 영문/숫자/한글 1~45자 이내이며 유저 계정은 기존 User table 에 있는지 중복체크를 한다. 
	 */
	@RequestMapping(value = "/v1/regist", method = RequestMethod.POST)
	public User regist(User user) {
		if (userService.isInvalidRegistration(user.getUserId(), user.getUserName())) {
			log.info(USER_ID_NAME_IS_ILLEGAL + " : {}, {}", user.getUserId(), user.getUserName());
			throw new IllegalArgumentException(USER_ID_NAME_IS_ILLEGAL);
		}

		return userService.regist(user);
	}

	/**
	 * 5. 친구 신청
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구신청자가 친구수락자에게 친구 신청을 보내고 친구 신청 이력을 리턴받는다. 리턴받는 항목은 아직 친구 수락 전이므로 수락여부(acceptYn=n), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=n), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=n) 이다. 추후, 친구수락자가 승인을 해야 수락 및 팔로우 모두 y로 바뀐다.
	 */
	@RequestMapping(value = "/v1/friend", method = RequestMethod.POST)
	public Friend apply(Friend friend) {
		if (friendService.isInvalidApply(friend)) {
			log.info(APPLIER_OR_ACCEPTOR_ILLEGAL_OR_EXIST_FRIEND + " : {}, {}", friend.getApplierId(), friend.getAcceptorId());
			throw new IllegalArgumentException(APPLIER_OR_ACCEPTOR_ILLEGAL_OR_EXIST_FRIEND);
		}

		return friendService.apply(friend);
	}

	/**
	 * 6. 친구 수락
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구수락자가 친구신청자의 친구 신청을 수락하며 쌍방간에 팔로우가 활성화 된다. 친구수락여부(acceptYn=y), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=y), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=y) 이다. 
	 */
	@RequestMapping(value = "/v1/friend", method = RequestMethod.PUT)
	public Friend accept(Friend friend) {
		if (friendService.notExistNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId())) {
			log.info(NOT_FRIEND_EACH_OTHER + " : {}, {}", friend.getApplierId(), friend.getAcceptorId());
			throw new IllegalStateException(NOT_FRIEND_EACH_OTHER);
		}

		return friendService.accept(friend);		
	}

	/**
	 * 7. 친구 끊기
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구 관계가 끊긴다면 친구신청자, 친구수락자 서로를 팔로우 하지 않고 친구 관계 여부도 n 으로 변경된다.
	 */
	@RequestMapping(value = "/v1/friend", method = RequestMethod.DELETE)
	public Friend end(Friend friend) {
		if (friendService.isInvalidAccepted(friend)) {
			log.info(NOT_FRIEND_EACH_OTHER + " : {}, {}", friend.getApplierId(), friend.getAcceptorId());
			throw new IllegalStateException(NOT_FRIEND_EACH_OTHER);
		}

		return friendService.end(friend);
	}

	/**
	 * 8. 친구 관계에 있는 사람 팔로우 맺기
	 * 친구 관계가 수락된다면 자동으로 서로 팔로우는 맺어지지만, 친구 관계더라도 팔로우는 끊고 다시 맺을 수 있기 때문에 팔로우 맺는 기능이 있어야 한다.
	 * 단, Friend 객체(Table) 에서 팔로우의 방향성(친구신청자->친구수락자를, 친구수락자->친구신청자를)이 있기 때문에 팔로우 맺기를 희망한 주체가 누구인지 follower 항목을 파라미터로 받는다.
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @param 	follower : 팔로우의 주체자, 'applierId' 값이 넘어온다면 친구신청자가 친구수락자를 팔로우 한다.
	 * @return	친구 관계끼리 팔로우가 맺어진 객체를 리턴받는다.
	 */
	@RequestMapping(value = "/v1/follow", method = RequestMethod.POST)
	public Friend follow(Friend friend, String follower) {
		if (friendService.isInvalidFollowAgent(follower) || friendService.isInvalidUnFollower(friend, follower)) {
			log.info(INVALID_FRIEND_AND_ABUSE_MESSAGE + " : {}, {}", friend.getApplierId(), friend.getAcceptorId());
			throw new IllegalStateException(INVALID_FRIEND_AND_ABUSE_MESSAGE);
		}

		return friendService.follow(friend, follower);
	}

	/**
	 * 9. 친구 관계에 있는 사람 팔로우 끊기
	 * 친구 관계이면 자동으로 팔로우가 맺어진 상태이며, 해당 유저의 타임라인에는 친구들의 Post 목록을 받아볼 수 있다.
	 * Post 목록을 받아보지 않기 위해 팔로우 끊기 기능이 존재한다.
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @param 	unfollower : 팔로우 끊기의 주체자, 'applierId' 값이 넘어온다면 친구신청자가 친구수락자를 팔로우를 끊는다.
	 * @return	친구 관계끼리 팔로우가 맺어진 객체를 리턴받는다.
	 */
	@RequestMapping(value = "/v1/follow", method = RequestMethod.DELETE)
	public Friend unFollow(Friend friend, String unfollower) {
		if (friendService.isInvalidFollowAgent(unfollower) || friendService.isInvalidFollwer(friend, unfollower)) {
			log.info(INVALID_FRIEND_AND_ABUSE_MESSAGE + " : {}, {}", friend.getApplierId(), friend.getAcceptorId());
			throw new IllegalStateException(INVALID_FRIEND_AND_ABUSE_MESSAGE);
		}

		return friendService.unfollow(friend, unfollower);
	}

	/**
	 * 10. Post 작성
	 * Post 의 작성은 자기 자신 또는 친구의 타임라인에 작성할 수 있으며 이후 뉴스피드 목록에 노출될 영역이다.
	 * 그리고, 자기 자신의 소유인 타임라인과 타임라인이 모인 뉴스피드와 구분될 수 있도록 Post 의 주인을 명시하는 ownerId 프로퍼티가 존재한다.
	 * 
	 * @param 	post	: ownerId(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
	 * @return	작성된 Post 의 객체를 리턴한다.
	 */
	@RequestMapping(value = "/v1/post", method = RequestMethod.POST)
	public Post write(Post post) {
		if (postService.isInvalidWrite(post)) {
			log.info(INVALID_FRIEND_MESSAGE + " : {}, {}", post.getOwnerId(), post.getCreatorId());
			throw new IllegalStateException(INVALID_FRIEND_MESSAGE);
		} 

		return postService.write(post);
	}

	/**
	 * 11. Post 수정
	 * 
	 * @param	post 객체의 날짜를 제외한 모든 항목을 받는다.
	 * @return	수정된 Post 객체를 리턴한다.
	 */
	@RequestMapping(value = "/v1/post", method = RequestMethod.PUT)	
	public Post modify(Post post) {
		if (postService.isInvalidModify(post)) {
			log.info(INVALID_FRIEND_MESSAGE + " : {}, {}", post.getOwnerId(), post.getModifierId());
			throw new IllegalStateException(INVALID_FRIEND_MESSAGE);
		}

		return postService.modify(post);
	}

	/**
	 * 12. Post 삭제
	 * 
	 * @param post 객체 항목을 받는다.
	 */
	@RequestMapping(value = "/v1/post", method = RequestMethod.DELETE)
	public void delete(Post post) {
		if (postService.isInvalidDelete(post)) {
			log.info(INVALID_FRIEND_MESSAGE + " : {}, {}", post.getOwnerId(), post.getModifierId());
			throw new IllegalStateException(INVALID_FRIEND_MESSAGE);
		}

		postService.delete(post);		
	}

	/**
	 * 예외 발생시 메세지 세팅
	 * 
	 * @param exception
	 * @return
	 */
	@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage exceptionMessage(Exception exception) {
		String message = StringUtils.defaultIfBlank(exception.getMessage(), SERVER_ERROR);

		return new ExceptionMessage(message);
	}
}