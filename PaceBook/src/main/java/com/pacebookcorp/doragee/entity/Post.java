package com.pacebookcorp.doragee.entity;

import java.util.Date;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kwon Young
 */
@Entity
@Table(name = "t_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {	
	@Id
	@Column(name = "post_pk")
	private String postPk;
	
	@Column(name = "ownr_id")
	private String ownerId;
	
	@Column(name = "content")
	private String content;
	
	@Column(name = "crer_id")
	private String creatorId;

	@Column(name = "cre_ymdt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "modr_id")
	private String modifierId;

	@Column(name = "mod_ymdt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

}