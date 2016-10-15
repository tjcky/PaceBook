# 사용 기술
언어 : Java 1.8 ( JPA / JPQL / Lombok )
RDBMS : MySQL
프레임워크 : Spring boot
빌드 : Maven
WAS : Tomcat 8

# 대표 기능 명세
팔로우(follow) : 다른 유저의 글을 보는 행위, 친구 관계가 성립된 유저끼리 한해서 해당되는 기능이다. 친구 관계가 성립되면 유저끼리 팔로우 기능은 자동으로 활성화되고, 친구 관계더라도 어느 한쪽이 다른 친구를 팔로우하는 기능을 비활성화 해두면 그 친구의 글은 보이지 않는다. 
포스팅(posting) : 유저가 자신 또는 친구의 타임라인에 글을 게시하는 행위로 친구 관계이고 팔로우 상태라면 타임라인에 게시된 글이 뉴스피드 영역에 노출된다. 
뉴스피드(news feed) : 타임라인에 게시된 글이 모여 뉴스피드를 형성하는데 유저가 뉴스피드에 모여진 글은 자기가 자신에게 남긴 글, 자신이 친구에게 남긴 글, 친구가 자신에게 남긴 글이 해당된다.
3-1. 타임라인(time line) : 특정 유저만의 ‘뉴스피드’, 본 개발에서는 타임라인을 뉴스피드와 묶어 하나의 테이블에 관리하지만 작성자의 주인(ownerId) 컬럼을 두어 타임라인과 뉴스피드를 구별한다.

# 상세 기능 명세
TODO : Post 를 ‘글’로 바꾼다. 신청자 -> 친구신청자
모든 사용자 정보 조회
- /v1/users
- GET

특정 유저의 뉴스피드 조회
- /v1/newsfeed/{userId} 
- GET
- userId 와 친구관계를 맺은 사람들이 작성한 글 목록을 가져오는데, 친구 관계더라도 팔로우가 활성화 된 친구의 글만 가져온다. 정렬 기준은 Post 가 수정된 날짜의 최신순 이다.
특정 유저의 타임라인 조회
- /v1/timeline/{userId} 
- GET
- userId 의 타임라인에 게시된 글을 가져오는데, 자신이 작성한 것과 친구가 자신에게 남긴 글을 가져온다. 

유저 가입
- /v1/regist 
- userId(계정 ID), userName(이름)
- POST
- id 와 이름만 입력 받아(패스워드 생략) 유효성 체크, 중복 체크 후 User 테이블에 입력된다.

친구 신청
/v1/friend
applierId (친구신청자), acceptorId (친구수락자)
POST
친구신청자가 친구수락자에게 친구 신청을 보내고 친구 신청 이력을 리턴받는다. 리턴받는 항목은 아직 친구 수락 전이므로 친구수락여부(acceptYn=n), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=n), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=n) 이다. 추후, 친구수락자가 승인을 해야 수락 및 팔로우 모두 y로 바뀐다.

친구 수락
/v1/friend
applierId (친구신청자), acceptorId (친구수락자)
PUT
친구수락자가 친구신청자의 친구 신청을 수락하며 친구신청자, 친구수락자 쌍방간에 팔로우가 활성화 된다. 친구수락여부(acceptYn=y), 친구신청자가 친구수락자를 팔로우 여부(applierFollowYn=y), 친구수락자가 친구신청자를 팔로우 여부(acceptFollowYn=y) 이다.
친구 수락 거부 기능은 생략
친구 끊기
/v1/friend
applierId (친구신청자), acceptorId (친구수락자)
DELETE
이미 친구 관계에 있는 유저끼리 해당되며 친구 관계가 끊긴다면 당연히 친구신청자, 친구수락자 간에 서로를 팔로우 하지 않고 단순히 활성화 상태값만 n 으로 바꾼다.

친구 관계에 있는 사람 팔로우 맺기
/v1/follow
applierId (친구신청자), acceptorId (친구수락자), follower(팔로우 맺기 희망한 주체)
POST
친구 관계가 수락된다면 자동으로 서로 팔로우는 맺어지지만, 친구 관계더라도 팔로우는 끊고 다시 맺을 수 있기 때문에 팔로우 맺는 기능이 있어야 한다. 단, Friend 객체(Table) 에서 팔로우의 방향성(친구신청자->친구수락자를, 친구수락자->친구신청자를)이 있기 때문에 팔로우 맺기를 희망한 주체가 누구인지 follower 항목을 파라미터로 받는다.
예를 들어 follower 항목 값이 applierId 가 넘어온다면 친구신청자(applierId)가 친구수락자(acceptorId)를 팔로우 한다.
친구 관계에 있는 사람 팔로우 끊기
/v1/follow
applierId (친구신청자), acceptorId (친구수락자), unfollower(팔로우 끊기 희망한 주체)
DELETE
친구 관계이면 자동으로 팔로우가 맺어진 상태이며, 해당 유저의 타임라인에는 친구들의 글 목록을 받아볼 수 있다. 글 목록을 받아보지 않기 위해 팔로우 끊기 기능이 존재한다. 단, 친구 관계는 유지되어서 친구의 타임라인에 직접 접속하면 볼 수 있다.
예를 들어, follower 항목 값이 applierId 가 넘어온다면 친구신청자(applierId)가 친구수락자(acceptorId)를 팔로우를 하지 않는다.

 Post 작성
/v1/post
ownerId(Post의 주인), creatorId(작성자 : Post의 주인, 친구), content(내용)
POST
글의 작성은 자기 자신 또는 친구의 타임라인에 작성할 수 있으며 이후 뉴스피드 목록에 노출될 영역이다. 그리고, 자기 자신의 소유인 타임라인과 타임라인이 모인 뉴스피드와 구분될 수 있도록 글의 주인을 명시하는 ownerId 프로퍼티가 존재한다.

 Post 수정
/v1/post
postPk, ownerId(Post의 주인), creatorId(작성자 : Post의 주인, 친구), modifierId(수정자), content(내용)
PUT
글의 수정은 유저가 자신에게 남긴 글과 자신이 다른 친구에게 남긴 글만 수정할 수 있다.
 Post 삭제 
/v1/post
postPk, ownerId(Post의 주인), modifierId(수정자)
DELETE
글의 삭제는 유저가 자신에게 남긴 글과 자신이 다른 친구에게 남긴 글만 삭제할 수 있다.
# 테이블 명세
t_user / 유저 테이블 / 설명 생략
CREATE TABLE `t_user` (
  `user_id` varchar(45) NOT NULL,
  `user_nm` varchar(45) NOT NULL,
  `crer_id` varchar(45) NOT NULL,
  `cre_ymdt` datetime NOT NULL,
  `modr_id` varchar(45) NOT NULL,
  `mod_ymdt` datetime NOT NULL,
  PRIMARY KEY (`user_id`)
) DEFAULT CHARSET=utf8 COMMENT='pacebook 유저 테이블';
t_post / 포스트 테이블  
CREATE TABLE `t_post` (
  `post_pk` varchar(40) NOT NULL,
  `ownr_id` varchar(45) NOT NULL,
  `content` varchar(500) NOT NULL,
  `crer_id` varchar(45) NOT NULL,
  `cre_ymdt` datetime NOT NULL,
  `modr_id` varchar(45) NOT NULL,
  `mod_ymdt` datetime NOT NULL,
  PRIMARY KEY (`post_pk`,`ownr_id`)
) DEFAULT CHARSET=utf8 COMMENT='pacebook 포스팅 테이블';

‘게시된 글’이 저장되는 테이블
타임라인(유저 개인영역)과 타임라인이 모인 뉴스피드를 구분짓기 위하여 ownr_id(Post 의 주인) 프로퍼티가 존재한다.
게시글(content) 500 byte 이하
그 외, PK / 생성자 /  생성일 / 수정자 / 수정일 
t_frnd / 친구 테이블 
CREATE TABLE `t_frnd` (
  `frnd_pk` varchar(45) NOT NULL,
  `aply_id` varchar(45) NOT NULL,
  `accp_id` varchar(45) NOT NULL,
  `accp_yn` char(1) NOT NULL DEFAULT 'N',
  `aply_folw_yn` char(1) NOT NULL DEFAULT 'N',
  `accp_folw_yn` char(1) NOT NULL DEFAULT 'N',
  `cre_ymdt` datetime DEFAULT NULL,
  `mod_ymdt` datetime DEFAULT NULL,
  PRIMARY KEY (`frnd_pk`)
) DEFAULT CHARSET=utf8;

친구 관계 테이블
A 라는 유저가 B 라는 유저에게 친구를 신청했을 때부터 데이터가 인입된다.
이때 A는 친구신청자(aply_id), B는 친구수락자(accp_id) 이다.
B 가 A의 신청을 수락한 시점부터 A-B 의 친구관계(accp_yn) 는 y로 바뀐다.
또한, 팔로우 여부(친구신청자가 친구수락자를 팔로우 = aply_folw_yn, 친구수락자가 친구신청자를 = accp_folw_yn) 항목도 같이 저장하며, 역시 친구 신청을 수락한 시점부터 모두 활성화(y)로 바뀐다.
친구 관계를 끊는다면 accp_yn / aply_folw_yn / accp_folw_yn 을 비활성화(n)로 바꿀뿐 데이터는 삭제하지 않는다.
그 외, PK / 생성일 / 수정일

# 테스트 시나리오 / 테스트 결과
사용자 7명 가입(A, B, C, D, E, F, G) / Pass
가입된 사용자 전체 목록 조회 / Pass
사용자간 친구 신청, 수락
한 명도 친구가 없는 사용자(A) / 테스트 없음
자기가 자신에게 친구 신청(어뷰징) / Pass / 오류 메시지 노출
B가 C에게 신청한 상태인데 C가 B에게 친구 신청 시도(어뷰징) 
/ Pass / 오류 메시지 노출
A를 제외한 모든 사용자를 친구로 맺은 사용자(B) / Pass
그외, 일부 몇 명만 친구를 가진 사용자 (C, D, E, F, G) / Pass
각 사용자들, 자신의 타임라인에 포스팅 / Pass
각 사용자들, 친구의 타임라인에 포스팅 / Pass
각 사용자들, 자신의 타임라인 조회 / Pass
각 사용자들, 뉴스피드 조회 / Pass
일부 사용자, 친구 관계 팔로우 끊기 / Pass
일부 사용자, 친구 관계 팔로우 맺기 / Pass
각 사용자들, 자신의 타임라인에 게시한 포스트 수정 / Pass
각 사용자들, 친구의 타임라인에 게시한 포스트 수정 / Pass
각 사용자들, 자신의 타임라인에 게시한 포스트 삭제 / Pass
각 사용자들, 친구의 타임라인에 게시한 포스트 삭제 / Pass
 각 사용자들, 자신의 타임라인 조회 / Pass
 각 사용자들, 뉴스피드 조회 / Pass
 각 사용자들, 친구 관계가 아닌 사용자의 타임라인에 게시된 포스팅 시도(어뷰징)
/ Pass / 오류 메시지 노출
 각 사용자들, 친구 관계가 아닌 사용자의 타임라인에 게시된 포스팅 수정 시도(어뷰징)
/ Pass / 오류 메시지 노출
 각 사용자들, 친구 관계가 아닌 사용자의 타임라인에 게시된 포스팅 삭제 시도(어뷰징)
/ Pass / 오류 메시지 노출
