package com.pacebookcorp.doragee.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kwon Young
 */
@Entity
@Table(name = "t_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "user_nm")
	private String userName;

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