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
@Table(name = "t_frnd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
	@Id
	@Column(name= "frnd_pk")
	private String friendPk;
	
	@Column(name = "aply_id")
	private String applierId; 		// 친구 신청자

	@Column(name = "accp_id")
	private String acceptorId; 		// 친구 수락자

	@Column(name = "accp_yn")
	private String acceptYn; 		// 친구 관계 여부

	@Column(name = "aply_folw_yn")
	private String applierFollowYn;	// 신청자가 수락자를 팔로우 여부

	@Column(name = "accp_folw_yn")
	private String acceptFollowYn;	// 수락자가 신청자를 팔로우 여부

	@Column(name = "cre_ymdt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "mod_ymdt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;
}