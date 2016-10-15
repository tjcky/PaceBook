package com.pacebookcorp.doragee.service;

import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pacebookcorp.doragee.entity.Friend;
import com.pacebookcorp.doragee.repository.FriendRepository;
import com.pacebookcorp.doragee.repository.UserRepository;
import com.pacebookcorp.doragee.util.PaceBookUtils;

/**
 * PaceBook 서비스의 친구 관련 기능(맺기, 끊기, 팔로우, 언팔로우)을 담당하는 클래스
 * 
 * @author Kwon Young
 */
@Service
public class FriendService {
	private static final int VALID_APLLY_FRIEND_COUNT = 2;
	private static final String FRIEND_PK_PREFIX = "frnd";

	private static final String APPLIERID = "applierId";
	private static final String ACCEPTORID = "acceptorId";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FriendRepository friendRepository;

	/**
	 * 친구를 신청하기전 신청자, 수락자의 유효성 체크를 한다.
	 * 1. 첫번째 IF : 아이디의 유효성,
	 * 2. 두번째 IF : 신청자 및 수락자 모두 User Table 에 존재해야 하므로 리턴값이 2명이어야 한다,
	 * 3. 세번째 IF : 기존에 둘중 하나가 친구 신청한 이력이 없어야 한다.
	 *  
	 * @param 	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	true 를 리턴한다면 친구 신청이 가능하다.
	 */
	public boolean isValidApply(Friend friend) {
		String applierId = friend.getApplierId();
		String acceptorId = friend.getAcceptorId();

		if (PaceBookUtils.isInvalidUserIds(applierId, acceptorId)) {
			return false;
		}

		if (CollectionUtils.size(userRepository.findUsers(applierId, acceptorId)) != VALID_APLLY_FRIEND_COUNT) {
			return false;
		}
 
		if (Objects.isNull(friendRepository.existFriendRelation(applierId, acceptorId)) == false) {
			return false;
		}

		return true;
	}

	/**
	 * isValidApplyFriend 의 부정 함수 : 친구 신청 불가능 여부를 리턴한다.
	 * 
	 * @param 	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	true 를 리턴한다면 친구 신청이 불가능하다.
	 */
	public boolean isInvalidApply(Friend friend) {
		return isValidApply(friend) == false;
	}
	
	/**
	 * 5. 친구 신청
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구신청자가 친구수락자에게 친구 신청을 보내고 친구 신청 이력을 리턴받는다. 리턴받는 항목은 아직 친구 수락 전이므로 수락여부(acceptYn=n), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=n), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=n) 이다. 추후, 친구수락자가 승인을 해야 수락 및 팔로우 모두 y로 바뀐다.
	 */
	public Friend apply(Friend friend) {
		setCreate(friend);

		return friendRepository.save(friend);
	}	

	/**
	 * Friend 테이블에 입력될 친구관계 객체 정보
	 * 
	 * @param 	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	신청자가 수락자에게 친구 신청을 보낸 상태를 리턴받고 리턴받는 항목은 수락여부(n), 신청자가 수락자를 팔로우 여부(n), 수락자가 신청자를 팔로우 여부(n) 이다. 추후, 수락자가 승인을 해야 수락 및 팔로우 모두 y로 바뀐다.
	 */
	private void setCreate(Friend friend) {
		friend.setFriendPk(PaceBookUtils.generatePrivateKey(FRIEND_PK_PREFIX));
		friend.setAcceptYn(PaceBookUtils.NO);
		friend.setAcceptFollowYn(PaceBookUtils.NO);
		friend.setApplierFollowYn(PaceBookUtils.NO);
		friend.setCreatedDate(PaceBookUtils.nowDateTime());
		friend.setModifiedDate(PaceBookUtils.nowDateTime());
	}

	/**
	 * 친구 신청을 수락하기 전 유효성 체크
	 * 첫번째 IF : 신청자, 수락자 아이디의 유효성 체크,
	 * 두번째 IF : 친구 신청이 수락에 앞서 먼저 친구 신청한 이력이 있어야 한다.
	 * 
	 * @param	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	친구 신청한 이력이 있다면 true 를 리턴한다.
	 */
	public boolean existNotAccptedFriendRelation(String applierId, String acceptorId) {
		if (PaceBookUtils.isInvalidUserIds(applierId, acceptorId)) {
			return false;
		}

		if (Objects.isNull(friendRepository.findNotAccptedFriendRelation(applierId, acceptorId))) {
			return false;
		}

		return true;
	}

	/**
	 * existNotAccptedFriendRelation 의 부정 함수
	 * 
	 * @param	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	친구 신청한 이력이 없다면 true 를 리턴한다.
	 */
	public boolean notExistNotAccptedFriendRelation(String applierId, String acceptorId) {
		return existNotAccptedFriendRelation(applierId, acceptorId) == false;
	}

	/**
	 * 6. 친구 수락
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구수락자가 친구신청자의 친구 신청을 수락하며 쌍방간에 팔로우가 활성화 된다. 친구수락여부(acceptYn=y), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=y), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=y) 이다. 
	 */
	public Friend accept(Friend friend) {
		Friend findFriend = friendRepository.findNotAccptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		setFriendStatus(findFriend, PaceBookUtils.YES);

		return friendRepository.save(findFriend);
	}

	/**
	 * 친구 관계의 항목값을 변경한다.
	 * 
	 * @param friend	: 친구 수락, 신청자가 수락자를 팔로우, 수락자가 신청자를 팔로우 여부를 변경한다.
	 * @param acceptYn	: 수락(y), 끊기(n)
	 */	
	private void setFriendStatus(Friend friend, String acceptYn) {
		friend.setAcceptYn(acceptYn);
		friend.setApplierFollowYn(acceptYn);
		friend.setAcceptFollowYn(acceptYn);
		friend.setModifiedDate(PaceBookUtils.nowDateTime());
	}
	
	/**
	 * 친구 관계를 끊거나 언팔, 팔로우 하기 위해선 먼저 '친구' 였는지부터 검사하는 함수
	 * 기존에 친구 관계라면 true 를 리턴한다.
	 * 첫번째 IF : 아이디의 유효성 체크
	 * 두번째 IF : 기존에 친구 관계가 존재해야 언팔, 및 친구 관계를 끊을 수 있다.
	 * 
	 * @param	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	
	 */
	public boolean isValidAccepted(Friend friend) {
		String applierId = friend.getApplierId();
		String acceptorId = friend.getAcceptorId();	
		
		if (PaceBookUtils.isInvalidUserIds(applierId, acceptorId)) {
			return false;
		}

		if (Objects.isNull(friendRepository.findAcceptedFriendRelation(applierId, acceptorId))){
			return false;
		}
		
		return true;
	}

	/**
	 * isValidAcceptedFriend 의 부정 함수
	 * 
	 * @param	friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @return	기존에 친구 관계가 아니라면 true 를 리턴한다.
	 */
	public boolean isInvalidAccepted(Friend friend) {
		return isValidAccepted(friend) == false;
	}
	
	/**
	 * 팔로우를 맺기전 친구 관계에서 팔로우 여부가 아닌지 체크하는 함수
	 * 
	 * @param friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @param follower	팔로우 주체를 리턴받는다.
	 * @return
	 */
	public boolean isValidUnFollower(Friend friend, String follower) {
		String applierId = friend.getApplierId();
		String acceptorId = friend.getAcceptorId();	

		if (PaceBookUtils.isInvalidUserIds(applierId, acceptorId)) {
			return false;
		}

		if (StringUtils.equals(APPLIERID, follower) && Objects.isNull(friendRepository.existApplierIsUnFollower(applierId, acceptorId)) == false) {
			return true;
		}

		if (StringUtils.equals(ACCEPTORID, follower) && Objects.isNull(friendRepository.existAcceptorIsUnFollower(applierId, acceptorId)) == false) {
			return true;
		}
		
		return false;
	}

	/**
	 * isValidUnFollower 의 부정함수
	 * 
	 * @param friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @param follower	 팔로우 주체를 리턴받는다.
	 */
	public boolean isInvalidUnFollower(Friend friend, String follower) {
		return isValidUnFollower(friend, follower) == false;
	}
	/**
	 * 팔로우를 끊기전 친구 관계에서 팔로우 여부인지 체크하는 함수 
	 * 
	 * @param friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @param unfollower	언팔할 주체를 리턴받는다.
	 * @return
	 */
	public boolean isValidFollower(Friend friend, String unfollower) {
		String applierId = friend.getApplierId();
		String acceptorId = friend.getAcceptorId();	

		if (PaceBookUtils.isInvalidUserIds(applierId, acceptorId)) {
			return false;
		}

		if (StringUtils.equals(APPLIERID, unfollower) && Objects.isNull(friendRepository.existApplierIsFollower(applierId, acceptorId)) == false) {
			return true;
		}

		if (StringUtils.equals(ACCEPTORID, unfollower) && Objects.isNull(friendRepository.existAcceptorIsFollower(applierId, acceptorId)) == false) {
			return true;
		}
		
		return false;
	}

	/**
	 * isValidFollower 의 부정함수
	 * 
	 * @param friend	 : applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @param unfollower	언팔할 주체를 리턴받는다.
	 */
	public boolean isInvalidFollwer(Friend friend, String unfollower) {
		return isValidFollower(friend, unfollower) == false;
	}
	/**
	 * Follower 의 주체가 신청자인지 수락자인지 명확해야 한다. 파라메터 어뷰징 방지 가드로직
	 * 
	 * @param 	follower	: applierId or acceptorId 
	 * @return	주체가 신청자, 수락자 중 하나라면 true 를 리턴한다.
	 */
	public boolean isValidFollowAgent(String follower) {
		if (StringUtils.equals(APPLIERID, follower)) {
			return true;
		}

		if (StringUtils.equals(ACCEPTORID, follower)) {
			return true;
		}
		
		return false;
	}

	/**
	 * isValidFollowAgent 의 부정 함수
	 * 
	 * @param 	follower	: applierId or acceptorId 
	 * @return	주체가 신청자, 수락자도 아닌 어뷰징 파라미터라면 true 를 리턴한다.
	 */
	public boolean isInvalidFollowAgent(String follower) {
		return isValidFollowAgent(follower) == false;
	}

	/**
	 * 7. 친구 끊기
	 * 
	 * @param	friend	 : applierId(친구신청자), acceptorId(친구수락자) 만 입력받는다.
	 * @return	친구 관계가 끊긴다면 친구신청자, 친구수락자 서로를 팔로우 하지 않고 친구 관계 여부도 n 으로 변경된다.
	 */
	public Friend end(Friend friend) {
		Friend endfriend = friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		setFriendStatus(endfriend, PaceBookUtils.NO);

		return friendRepository.save(endfriend);
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
	public Friend follow(Friend friend, String follower) {
		Friend followFriend = friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		setFollowFriendStatus(followFriend, follower, PaceBookUtils.YES);

		return friendRepository.save(followFriend);
	}

	/**
	 * 친구 관계에서 팔로우 항목값을 변경한다.
	 *  	
	 * @param 	friend	 		: applierId(신청자), acceptorId(수락자) 만 입력받는다.
	 * @param 	follower		: 팔로우의 주체(applierId, acceptorId)
	 * @param 	followStatus	: 팔로우 여부(y/n)
	 */
	private void setFollowFriendStatus(Friend friend, String follower, String followStatus) {	
		if (StringUtils.equals(APPLIERID, follower)) {
			friend.setApplierFollowYn(followStatus);
		} else {
			friend.setAcceptFollowYn(followStatus);			
		}

		friend.setModifiedDate(PaceBookUtils.nowDateTime());
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
	public Friend unfollow(Friend friend, String unfollower) {
		Friend unFollowFriend = friendRepository.findAcceptedFriendRelation(friend.getApplierId(), friend.getAcceptorId());

		setFollowFriendStatus(unFollowFriend, unfollower, PaceBookUtils.NO);

		return friendRepository.save(unFollowFriend);
	}
}