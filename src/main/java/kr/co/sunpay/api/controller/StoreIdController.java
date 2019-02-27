package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.service.StoreService;

@RestController
@RequestMapping("/store/id")
public class StoreIdController {

	@Autowired
	private StoreService storeService;
	
	@GetMapping("/{memberUid}")
	public List<StoreId>retrieveStoreIds(@PathVariable("memberUid") int memberUid, @RequestParam("storeUid") int storeUid) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}
		
		return storeService.getStoreIds(storeUid);
	}
	
	/**
	 * @param memberUid
	 * @param storeUid
	 * @param ids
	 * @return
	 */
	@PostMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value="상점ID 생성", notes="상점ID 생성 시 id, serviceTypeCode값만 적용됩니다.")
	public ResponseEntity<Object> createStoreIds(@PathVariable("memberUid") int memberUid,
			@PathVariable("storeUid") int storeUid,
			@ApiParam(value = "* id와 serviceTypeCode만 입력") @RequestBody List<StoreId> ids) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}
		
		Store store = storeService.createStoreIds(storeUid, ids);
		
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/store/{memberUid}/{uid}")
						.buildAndExpand(memberUid, store.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@DeleteMapping("/{memberUid}/{storeUid}/{storeId}/{storeId}")
	public void deleteStoreId(@PathVariable("memberUid") int memberUid, @PathVariable("storeUid") int storeUid, @PathVariable("storeId") String storeId) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}
		
		storeService.deleteStoreId(storeUid, storeId);
	}
	
	@PutMapping("/activate/{memberUid}/{storeUid}/{storeId}")
	@ApiOperation(value="상점ID 활성화", notes="상점ID 활성화")
	public void activate(@PathVariable("memberUid") int memberUid, @PathVariable("storeUid") int storeUid, @PathVariable("storeId") String storeId) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}
		
		storeService.activateStoreId(storeUid, storeId);
	}
	
	@PutMapping("/instantOn/{memberUid}/{storeUid}")
	@ApiOperation(value="순간정산 상점ID 활성화", notes="순간정산 활성화")
	public void instantOn(@PathVariable("memberUid") int memberUid, @PathVariable("storeUid") int storeUid) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}

		if (!storeService.instantOn(storeUid))
			throw new IllegalArgumentException("순간정산ID를 찾을 수 없습니다.");
	}
	
	@PutMapping("/instantOff/{memberUid}/{storeUid}")
	@ApiOperation(value="순간정산 상점ID 비활성화", notes="순간정산 비활성화, 일반정산으로 전환")
	public void instantOff(@PathVariable("memberUid") int memberUid, @PathVariable("storeUid") int storeUid) {
		
		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}

		if (!storeService.instantOff(storeUid))
			throw new IllegalArgumentException("일반정산ID를 찾을 수 없습니다.");
	}
}
