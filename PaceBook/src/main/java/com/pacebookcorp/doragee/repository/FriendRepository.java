package com.pacebookcorp.doragee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.pacebookcorp.doragee.entity.Friend;

/**
 * @author Kwon Young
 */
public interface FriendRepository extends JpaRepository<Friend, String> {
	@Query("SELECT x FROM Friend x WHERE x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.acceptYn='n'")
	Friend findNotAccptedFriendRelation(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE (x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.acceptYn='y') OR (x.applierId=:acceptorId AND x.acceptorId=:applierId AND x.acceptYn='y')")
	Friend findAcceptedFriendRelation(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.applierFollowYn='y'")
	Friend existApplierIsFollower(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.acceptFollowYn='y'")
	Friend existAcceptorIsFollower(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.applierFollowYn='n'")
	Friend existApplierIsUnFollower(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE x.applierId=:applierId AND x.acceptorId=:acceptorId AND x.acceptFollowYn='n'")
	Friend existAcceptorIsUnFollower(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);

	@Query("SELECT x FROM Friend x WHERE (x.applierId=:applierId AND x.acceptorId=:acceptorId) OR (x.applierId=:acceptorId AND x.acceptorId=:applierId) ")
	Friend existFriendRelation(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);
}