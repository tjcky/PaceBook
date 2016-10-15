package com.pacebookcorp.doragee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pacebookcorp.doragee.entity.Post;

/**
 * @author Kwon Young
 */
public interface PostRepository extends JpaRepository<Post, String> {
	@Query(value = "SELECT post.* "
				   + "FROM t_post post"
				   	  + ", ("
				   	  		+ "(SELECT accp_id AS user_id "
				   	  		   + "FROM t_frnd "
				   	  		  + "WHERE accp_yn = 'y' "
				   	  		  	+ "AND aply_folw_yn = 'y' "
				   	  		  	+ "AND aply_id=:userId"
				   	  		+ ") "
				   	  		+ "UNION ALL "
				   	  		+ "(SELECT aply_id AS user_id "
				   	  		   + "FROM t_frnd "
				   	  		  + "WHERE accp_yn = 'y' "
				   	  		    + "AND accp_folw_yn = 'y'"
				   	  		    + "AND accp_id=:userId"
				   	  		+ ") "
				   	  		+ "UNION ALL "
				   	  		+ "(SELECT user_id "
				   	  		   + "FROM t_user "
				   	  		  + "WHERE user_id=:userId"
				   	  		+ ") "
				   	  	+ ") frnd "
				  + "WHERE post.ownr_id = frnd.user_Id "
				  + "ORDER BY post.mod_ymdt DESC", nativeQuery = true) 
	List<Post> newsfeed(@Param("userId") String userId);

	@Query("SELECT p FROM Post p WHERE p.ownerId=:userId")
	List<Post> timeline(@Param("userId") String userId);
}