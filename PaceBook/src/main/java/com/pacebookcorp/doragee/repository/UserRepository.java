package com.pacebookcorp.doragee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pacebookcorp.doragee.entity.User;

/**
 * @author Kwon Young
 */
public interface UserRepository extends JpaRepository<User, String> {	
	@Query("SELECT x.userId FROM User x WHERE x.userId=:applierId OR x.userId=:acceptorId")
	List<String> findUsers(@Param("applierId") String applierId, @Param("acceptorId") String acceptorId);	
}