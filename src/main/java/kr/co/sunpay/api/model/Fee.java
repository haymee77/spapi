package kr.co.sunpay.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Fee {

	// PG 수수료
	@ApiModelProperty(notes="PG 수수료(납부할 수수료)")
	private Double pg;
	
	// 순간정산(송금) 수수료
	@ApiModelProperty(notes="순간정산(송금) 수수료(납부할 수수료)")
	private Integer transPg;
	
}
