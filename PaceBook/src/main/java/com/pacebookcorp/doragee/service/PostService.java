package com.pacebookcorp.doragee.service;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pacebookcorp.doragee.entity.Post;
import com.pacebookcorp.doragee.repository.FriendRepository;
import com.pacebookcorp.doragee.repository.PostRepository;
import com.pacebookcorp.doragee.util.PaceBookUtils;

/**
 * PaceBook 서비스의 Post 관련 기능을 담당하는 클래스(Post 의 CRUD)
 * 
 * @author Kwon Young
 */
@Service
public class PostService {	
	private static final String POST_PK_PREFIX = "post";
	private static final int MAX_CONTENT_SIZE = 500;
	
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private FriendRepository friendRepository;
	
	/**
	 * 2. 특정 유저의 뉴스피드 조회
	 * 
	 * @param 	userId 특정 유저 계정(id)
	 * @return	userId 와 친구관계를 맺은 사람들이 작성한 Post 목록을 가져오는데, 친구 관계더라도 팔로우가 활성화 된 친구의 Post만 가져온다. 정렬 기준은 Post 가 수정된 날짜 기준이다.(수정일이 작성일보다 최근)
	 */
	public List<Post> newsfeed(String userId) {
		return postRepository.newsfeed(userId);
	}
	
	/**
	 * 3. 특정 유저의 타임라인 조회
	 * 
	 * @param	userId 특정 유저 계정(id)
	 * @return	userId 의 타임라인에 게시된 Post를 가져오는데, 자신이 작성한 것과 친구가 자신에게 남긴 글을 가져온다. 
	 */
	public List<Post> timeline(String userId) {		
		return postRepository.timeline(userId);
	}

	/**
	 * Post 작성의 유효성 체크
	 * 첫번째 IF : 아이디 유효성
	 * 두번째 IF : 내용은 500자가 넘지 않아야 한다.
	 * 세번째 IF : 자기 자신에게 쓰는 것이라면 네번째 IF(친구 관계) 필요없이 true 리턴한다.
	 * 두번째 IF : 세번째 IF 를 통과했다면 친구에게 글을 남기는 것인데 친구 관계가 유효해야 한다.
	 * 
	 * @param 	post	: ownerId(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
	 * @return	Post 작성을 할 수 있다면 true 를 리턴한다.
	 */
	public boolean isValidWrite(Post post) {		
		String ownerId = post.getOwnerId();
		String creatorId = post.getCreatorId();
		String content = post.getContent();

		if (PaceBookUtils.isInvalidUserIds(ownerId, creatorId)) {
			return false;
		}

		if (StringUtils.length(content) >= MAX_CONTENT_SIZE) {
			return false;
		}
		
		if (StringUtils.equals(ownerId, creatorId)) {
			return true;
		}
 
		if (Objects.isNull(friendRepository.findAcceptedFriendRelation(ownerId, creatorId))) {
			return false;
		}
		
		return true;
	}

	/**
	 * isInvalidWritePost 의 부정 함수
	 * 
	 * @param 	post	: owner(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
	 * @return	Post 작성을 할 수 없다면 true 를 리턴한다.
	 */
	public boolean isInvalidWrite(Post post) {
		return isValidWrite(post) == false;
	}

	/**
	 * 10. Post 작성
	 * Post 의 작성은 자기 자신 또는 친구의 타임라인에 작성할 수 있으며 이후 뉴스피드 목록에 노출될 영역이다.
	 * 그리고, 자기 자신의 소유인 타임라인과 타임라인이 모인 뉴스피드와 구분될 수 있도록 Post 의 주인을 명시하는 ownerId 프로퍼티가 존재한다.
	 * 
	 * @param 	post	: ownerId(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
	 * @return	작성된 Post 의 객체를 리턴한다.
	 */
	public Post write(Post post) {		
		return postRepository.save(create(post));
	}

	/**
	 * Post 테이블에 입력될 객체를 리턴한다.
	 * 
	 * @param 	post	: owner(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
	 * @return	작성된 Post 의 객체를 리턴한다.
	 */
	private Post create(Post post) {
		Post createdPost = new Post();

		createdPost.setPostPk(PaceBookUtils.generatePrivateKey(POST_PK_PREFIX));
		createdPost.setOwnerId(post.getOwnerId());
		createdPost.setContent(post.getContent());
		createdPost.setCreatorId(post.getCreatorId());
		createdPost.setCreatedDate(PaceBookUtils.nowDateTime());
		createdPost.setModifierId(post.getCreatorId());
		createdPost.setModifiedDate(PaceBookUtils.nowDateTime());

		return createdPost;
	}

	/**
	 * Post 를 수정하기 전의 유효성 체크
	 * 글을 수정 시도자는 modifierId 이다.
	 * 
	 * 첫번째 IF : 수정 시도자가 작성자가 아니면 수정할 수 없다.
	 * 두번째 IF : PostPK 가 어뷰징이 아닌지 체크한다.
	 * 세번째 IF : 아이디의 유효성 체크
	 * 네번째 IF : Post 의 내용은 500자 이내여야 한다.
	 * 다섯째 IF : 수정자와 ownerId가 같다면 본인이 본인이 남긴 글을 수정하는 것이므로 바로 true 를 리턴한다.
	 * 마지막으로, 다섯번째 IF 를 통과했다면 친구가 남긴 글을 수정하는 것인데 친구관계가 성립되어있어야 한다.
	 * 
	 * @param 	post 객체의 날짜를 제외한 모든 항목을 받는다.
	 * @return	post 객체를 수정할 수 있다면 true 를 리턴한다.
	 */
	public boolean isValidModify(Post post) {
		String postPk = post.getPostPk();
		String ownerId = post.getOwnerId();
		String creatorId = post.getCreatorId();
		String modifierId = post.getModifierId();
		String content = post.getContent();
		
		if (StringUtils.equals(creatorId, modifierId) == false) {
			return false;
		}

		if (PaceBookUtils.isInvalidPrivateKey(postPk, POST_PK_PREFIX)) {
			return false;
		}

		if (PaceBookUtils.isInvalidUserIds(ownerId, modifierId)) {
			return false;
		}
		
		if (content.getBytes().length >= MAX_CONTENT_SIZE) {
			return false;
		}

		if (StringUtils.equals(ownerId, modifierId)) {
			return true;
		}

		return Objects.isNull(friendRepository.findAcceptedFriendRelation(ownerId, modifierId)) == false;
	}

	/**
	 * isValidModifyPost 의 부정 함수, Post 를 수정하기 전의 유효성 체크
	 * 
	 * @param 	post 객체의 날짜를 제외한 모든 항목을 받는다.
	 * @return	post 객체를 수정할 수 없다면 true 를 리턴한다.
	 */
	public boolean isInvalidModify(Post post) {
		return isValidModify(post) == false;
	}

	/**
	 * 11. Post 수정
	 * 
	 * @param	post 객체의 날짜를 제외한 모든 항목을 받는다.
	 * @return	수정된 Post 객체를 리턴한다.
	 */
	public Post modify(Post inputPost) {
		Post modifiedPost = postRepository.findOne(inputPost.getPostPk());

		if (Objects.isNull(modifiedPost)) {
			return new Post();
		}

		setModified(inputPost, modifiedPost);

		return postRepository.save(modifiedPost);
	}

	/**
	 * Post 를 수정할 객체를 세팅하며, 내용(content)과 수정일을 업데이트 한다.
	 * 
	 * @param post			수정할 내용으로 넘어온 Post 객체
	 * @param modifiedPost	DB에 저장될 Post 객체
	 */	
	private void setModified(Post post, Post modifiedPost) {
		modifiedPost.setContent(post.getContent());				
		modifiedPost.setModifierId(post.getModifierId());
		modifiedPost.setModifiedDate(PaceBookUtils.nowDateTime());
	}

	/**
	 * Post 를 삭제하기 전의 유효성 체크
	 * Post 는 본인이 본인 타임라인에 작성한 것을 삭제할 수 있으며, 삭제 시도자는 modifierId 이다.
	 * 첫번째 IF : PostPK 가 어뷰징이 아닌지 체크한다.
	 * 두번째 IF : 아이디의 유효성 체크
	 * 세번째 IF : 본인이 자신에게 쓴 글은 삭제 가능하다.
	 * 네번째 IF : 세번째 IF 를 통과했다면 친구가 남긴글인데 역시 친구관계가 유효해야 삭제할 수 있다.
	 * 
	 * @param 	post 객체
	 * @return	post 객체를 삭제할 수 있다면 true 를 리턴한다.
	 */
	public boolean isValidDelete(Post post) {
		String postPk = post.getPostPk();
		String ownerId = post.getOwnerId();
		String modifierId = post.getModifierId();	

		if (PaceBookUtils.isInvalidPrivateKey(postPk, POST_PK_PREFIX)) {
			return false;
		}

		if (PaceBookUtils.isInvalidUserIds(ownerId, modifierId)) {
			return false;
		}

		if (StringUtils.equals(ownerId, modifierId)) {
			return true;
		}

		if (Objects.isNull(friendRepository.findAcceptedFriendRelation(ownerId, modifierId))) {
			return false;
		}

		return true;
	}

	/**
	 * isValidDeletePost 의 부정 함수, Post 를 삭제하기 전의 유효성 체크
	 * 
	 * @param 	post 객체
	 * @return	post 객체를 수정할 수 없다면 true 를 리턴한다.
	 */
	public boolean isInvalidDelete(Post post) {
		return isValidDelete(post) == false;
	}

	/**
	 * 12. Post 삭제
	 * 
	 * @param post 객체 항목을 받는다.
	 */
	public void delete(Post post) {
		Post deletePost = postRepository.findOne(post.getPostPk());

		postRepository.delete(deletePost);
	}
}