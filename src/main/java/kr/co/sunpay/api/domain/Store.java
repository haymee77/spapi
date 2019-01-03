package kr.co.sunpay.api.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "SP_STORES")
@Where(clause = "DELETED<>1")
@SQLDelete(sql = "UPDATE SP_STORES SET DELETED=1 WHERE UID=?")
@ToString
public class Store extends BaseEntity {

	@ApiModelProperty(notes="사업자 여부(1:사업자, 0:비사업자)")
	@Column(name="BIZ_FL", columnDefinition="BIT(1)")
	private byte bizFl;
	
	@ApiModelProperty(notes="은행코드")
	@Column(name="BANK_CD", length=20)
	private String bankCode;
	
	@ApiModelProperty(notes="계좌번호")
	@Column(name="BANK_ACCOUNT_NO", length=45)
	private String bankAccountNo;
	
	@ApiModelProperty(notes="계좌주명")
	@Column(name="BANK_ACCOUNT_NM", length=10)
	private String bankAccountName;
	
	@ApiModelProperty(notes="취소예치금 금액")
	@Column(name="DEPOSIT")
	private int depost;
	
	@ApiModelProperty(notes="취소예치금 입금번호")
	@Column(name="DEPOSIT_NO", length=10)
	private String depostNo;
	
	@ApiModelProperty(notes="사업자 등록번호")
	@Column(name="BIZ_NO", length=15)
	private String bizNo;
	
	@ApiModelProperty(notes="사업자 상호명")
	@Column(name="BIZ_NM", length=50)
	private String bizName;
	
	@ApiModelProperty(notes="사업자 성명")
	@Column(name="BIZ_OWNER", length=10)
	private String bizOwner;
	
	@ApiModelProperty(notes="사업자 주민번호")
	@Column(name="BIZ_OWNER_REGI_NO", length=15)
	private String bizOwnerRegiNo;
	
	@ApiModelProperty(notes="사업자 우편번호")
	@Column(name="BIZ_ZIPCODE", length=10)
	private String bizZipcode;
	
	@ApiModelProperty(notes="사업자 주소-기본")
	@Column(name="BIZ_ADDRESS_BASIC", length=100)
	private String bizAddressBasic;
	
	@ApiModelProperty(notes="사업자 주소-상세")
	@Column(name="BIZ_ADDRESS_DETAIL", length=100)
	private String bizAddressDetail;
	
	@ApiModelProperty(notes="사업자 연락처")
	@Column(name="BIZ_CONTACT", length=25)
	private String bizContact;
	
	@OneToMany(orphanRemoval=true)
	@JoinColumn(name="STORE_UID_FK")
	private List<StoreId> storeIds;
}
