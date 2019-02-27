package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import lombok.extern.java.Log;

@Log
@Service
public class StoreService extends MemberService {

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	StoreIdRepository storeIdRepo;
	
	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	GroupService groupService;
	
	@Autowired
	MemberService memberService;
	
	public static final String SERVICE_TYPE_INSTANT = "INSTANT";
	public static final String SERVICE_TYPE_D2 = "D2";
	
	/**
	 * 상점 데이터 검사기
	 * @param store
	 * @return
	 */
	public boolean validator(Store store) {
		
		// 상위 그룹 검사
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}

		// 소속 멤버 검사
		if (store.getMembers().size() != 1) {
			throw new IllegalArgumentException("Owner member should be one");
		} else {
			Member owner = store.getMembers().get(0);
			
			if (owner.getRoles() == null || owner.getRoles().size() < 1) {
				throw new IllegalArgumentException("Store member has no role.");
			}
			
			if (!memberService.hasRole(owner, MemberService.ROLE_STORE)) {
				throw new IllegalArgumentException("Store member should have STORE role.");
			}
		}

		return true;
	}
	
	/**
	 * memberUid로 접근 가능한 상점인 경우 상점 데이터 업데이트
	 * @param storeUid
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store update(int storeUid, Store store, int memberUid) {
		
		Store updatedStore = getStore(storeUid);

		// 상점 수정 정보 검사
		updateValidator(store);
		
		// 수정 가능한 항목만 수정함
		// 사업자정보
		updatedStore.setBizOwner(store.getBizOwner());
		updatedStore.setBizOwnerRegiNo(store.getBizOwnerRegiNo());
		updatedStore.setBizName(store.getBizName());
		updatedStore.setBizAddressBasic(store.getBizAddressBasic());
		updatedStore.setBizAddressDetail(store.getBizAddressDetail());
		updatedStore.setBizContact(store.getBizContact());
		
		// 설치비
		updatedStore.setInstallationFeeAgency(store.getInstallationFeeAgency());
		updatedStore.setInstallationFeeBranch(store.getInstallationFeeBranch());
		updatedStore.setInstallationFeeHead(store.getInstallationFeeHead());
		// 가입비
		updatedStore.setMembershipFeeAgency(store.getMembershipFeeAgency());
		updatedStore.setMembershipFeeBranch(store.getMembershipFeeBranch());
		updatedStore.setMembershipFeeHead(store.getMembershipFeeHead());
		// 관리비
		updatedStore.setMaintenanceFeeAgency(store.getMaintenanceFeeAgency());
		updatedStore.setMaintenanceFeeBranch(store.getMaintenanceFeeBranch());
		updatedStore.setMaintenanceFeeHead(store.getMaintenanceFeeHead());
		
		storeRepo.save(updatedStore);

		return updatedStore;
		
	}
	
	/**
	 * 상점 정보 수정 시 데이터 점검, 문제 시 throws exception
	 * @param store
	 */
	public void updateValidator(Store store) {
		
		if (isEmpty(store.getBizOwner())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 성명)");
		}
		
		if (isEmpty(store.getBizOwnerRegiNo())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 주민번호)");
		}
		
		if (isEmpty(store.getBizName())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 사업장명)");
		}
	}
	
	/**
	 * 관리 가능한 멤버인지 확인(상점 소속 그룹을 포함한 상위그룹의 멤버인지 확인)
	 * ** 상점의 매니저 권한은 {@link #isStoreManager(int, Store)} 로 확인
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean isAdminable(int memberUid, Store store) {
		// 상위 그룹 검사
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}
		
		// memberUid 권한으로 접근 가능한 그룹 리스트에 소속되는 상점인지 확인
		try {
			List<Group> managerGroups = groupService.getGroups(memberUid);
			
			for (Group g : managerGroups) {
				if (store.getGroup().getUid() == g.getUid()) {
					return true;
				}
			}
		} catch (Exception ex) {
			return false;
		}
		
		return false;
	}
	
	/**
	 * 상점 소속의 Manager 권한이 있는 멤버인지 확인
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean isStoreManager(int memberUid, Store store) {
		
		Member manager = memberService.getMember(memberUid);
		
		if (memberService.hasRole(manager, MemberService.ROLE_MANAGER)) {
			if (manager.getStoreUid() == store.getUid()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * memberUid 하위 소속으로 상점 생성
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store create(Store store, int memberUid) {
		
		if (isAdminable(memberUid, store)) {
			return create(store);
		} else {
			throw new IllegalArgumentException("memberUid의 권한으로 생성할 수 없는 그룹 소속입니다.");
		}
	}

	/**
	 * 상점 생성
	 * @param store
	 * @return
	 */
	public Store create(Store store) {

		log.info("-- StoreService.create called...");
		
		// 데이터 검사
		validator(store);
		
		// 상점 데이터 셋팅
		store.setDeposit(0);
		
		// 상점ID는 상점ID 등록 API를 이용해야 함
		store.setStoreIds(null);
		
		// OWNER 멤버 등록
		List<Member> members = new ArrayList<Member>();
		
		// 소유주 멤버 데이터 셋팅
		Member owner = store.getMembers().get(0);
		
		// 아이디 중복검사
		if (memberService.hasMember(owner.getId())) {
			throw new DuplicateKeyException("아이디 중복");
		}
		
		// 비밀번호 암호화
		owner.setPassword(pwEncoder.encode(owner.getPassword()));
		
		// OWNER 권한 확인(상점 생성 시 멤버는 STORE, MANAGER, OWNER 권한을 default로 갖는다)
		if (!memberService.hasRole(owner, MemberService.ROLE_MANAGER)
				|| !memberService.hasRole(owner, MemberService.ROLE_OWNER)) {
			throw new IllegalArgumentException("Store owner member should have OWNER and MANAGER roles.");
		}
		members.add(store.getMembers().get(0));
		store.setMembers(members);
		
		// <- 상점 생성 시 수수료 데이터 셋팅 시작
		Group group = groupService.getGroup(store.getGroup().getUid());
		store.setGroup(group);
		
		// - PG 수수료는 환경설정에서 가져옴
		store.setFeePg(groupService.getConfig().getFeePg());
		store.setTransFeePg(groupService.getConfig().getTransFeePg());
		
		switch (group.getRoleCode()) {
		case GroupService.ROLE_HEAD:
			if (!(store.getFeeHead() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			
			if (!(store.getTransFeeHead() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeBranch(0.0);
			store.setFeeAgency(0.0);
			store.setTransFeeBranch(0);
			store.setTransFeeAgency(0);
			break;

		case GroupService.ROLE_BRANCH:
			if (!(store.getFeeBranch() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			if (!(store.getTransFeeBranch() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeHead(group.getFeeHead());
			store.setFeeAgency(0.0);
			store.setTransFeeHead(group.getTransFeeHead());
			store.setTransFeeAgency(0);
			break;
			
		case GroupService.ROLE_AGENCY:
			if (!(store.getFeeAgency() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			if (!(store.getTransFeeAgency() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeHead(group.getFeeHead());
			store.setFeeBranch(group.getFeeBranch());
			store.setTransFeeHead(group.getTransFeeHead());
			store.setTransFeeBranch(group.getTransFeeBranch());
			break;
		}
		// 상점 생성 시 수수료 데이터 셋팅 끝 ->
		
		// 상점 생성 후 예치금 번호 생성
		Store newStore = storeRepo.save(store);
		newStore.setDepositNo(createDepositNo());

		return storeRepo.save(newStore);
	}
	
	public String createDepositNo() {
		int randNo = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
		String depositNo = String.valueOf(randNo);
		
		if (storeRepo.findByDepositNo(depositNo).isPresent()) {
			return createDepositNo();
		} 
		
		return String.valueOf(randNo);
	}
	
	public Store getStore(int storeUid) {
		
		Optional<Store> oStore = storeRepo.findByUid(storeUid);
		
		if (!oStore.isPresent()) {
			throw new EntityNotFoundException("There is no Store available");
		}
		
		Store store = oStore.get();
		store.setGroupName(store.getGroup().getBizName());
		store.setGroupUid(store.getGroup().getUid());
		
		return oStore.get();
	}

	/**
	 * groupUid가 가진 상점리스트 반환
	 * 
	 * @param groupUid
	 * @return
	 */
	public List<Store> getStoresByGroup(int groupUid) {

		List<Store> stores = new ArrayList<Store>();
		
		Group group = groupService.getGroup(groupUid);
		
		group.getStores().forEach(s -> {
			stores.add(s);
		});

		return stores;
	}
	
	/**
	 * group 및 하위 group의 모든 상점 반환
	 * @param group
	 * @return
	 */
	public List<Store> getStoresByGroup(Group group) {
		List<Store> stores = new ArrayList<Store>();
		
		// 자신 소속 상점 가져오기
		group.getStores().forEach(s -> {
			stores.add(s);
		});
		
		// 하위 그룹의 상점 가져오기
		List<Group> children = groupService.getChildren(group, true);
		
		if (children != null) {
			children.forEach(g -> {
				g.getStores().forEach(s -> {
					stores.add(s);
				});
			});
		}
		
		return stores;
	}

	/**
	 * member 권한으로 접근 가능한 모든 상점리스트 반환
	 * @param member
	 * @return
	 */
	public List<Store> getStoresByMember(Member member) {
		
		List<Store> stores = new ArrayList<Store>();
		
		// 최고관리자 또는 본사 멤버인 경우 모든 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_TOP)
				|| memberService.hasRole(member, MemberService.ROLE_HEAD)) {
			stores = getStoresByGroup(member.getGroup());
		}
		
		// 상점 멤버인 경우 해당 상점만 반환 
		if (memberService.hasRole(member, MemberService.ROLE_STORE)) {
			stores.add(getStore(member.getStoreUid()));
			return stores;
		}
		
		// 대리점 멤버인 경우 해당 대리점의 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {
			stores = groupService.getGroup(member.getGroupUid()).getStores();
		}
		
		// 지사 멤버인 경우 해당 지사와 하위 대리점 소속의 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {
			stores = getStoresByGroup(member.getGroup());
		}
		
		for (Store store : stores) {
			store.setGroupName(store.getGroup().getBizName());
			store.setGroupUid(store.getGroup().getUid());
		}
		
		return stores;
	}
	
	/**
	 * 순간정산 켜져있는지 확인
	 * @param storeId
	 * @return
	 */
	public boolean isInstantOn(String storeId) {

		if (storeId != null && storeIdRepo.findByIdAndActivated(storeId, true).isPresent())
			return true;

		return false;
	}
	
	/**
	 * 상점 ID로 상점 리턴
	 * @param storeId
	 * @return
	 */
	public Store getStoreByStoreId(String storeId) {
		
		StoreId id = storeIdRepo.findById(storeId).orElse(null);
		
		if (id != null) {
			Store store = storeRepo.findByStoreIds(id).orElse(null);
			return store;
		}
		
		return null;
	}
	
	/**
	 * 상점ID 리스트 리턴
	 * @param storeUid
	 * @return
	 */
	public List<StoreId> getStoreIds(int storeUid) {
		Store store = getStore(storeUid);
		return store.getStoreIds();
	}
	
	public Store createStoreIds(int storeUid, List<StoreId> ids) {

		List<StoreId> nIds = new ArrayList<StoreId>();
		
		// 중복 ID 확인
		for (StoreId id : ids) {
			if (getStoreId(id.getId()) != null) {
				throw new DuplicateKeyException("상점ID는 중복될 수 없습니다.");
			}
			
			// 상점ID 생성 시 ID, ServiceTypeCode 값만 받아서 생성함
			StoreId nId = new StoreId();
			nId.setId(id.getId());
			nId.setServiceTypeCode(id.getServiceTypeCode());
			nIds.add(nId);
		}
		
		// 상점 정보 가져오기
		Store store = getStore(storeUid);
		
		// 새 상점ID 리스트 추가
		store.getStoreIds().addAll(nIds);
		
		// ServiceTypeCode 중복 체크
		if (isStoreIdTypeDuplicated(store.getStoreIds()))
			throw new DuplicateKeyException("정산타입이 중복됩니다.");
		
		// 상점 정보 저장
		storeRepo.save(store);
		return store;
	}
	
	/**
	 * 상점ID 삭제
	 * @param storeUid
	 * @param storeId
	 */
	public void deleteStoreId(int storeUid, String storeId) {
		
		Store store = getStore(storeUid);
		
		if (store == null) throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");
		
		for (Iterator<StoreId> iter = store.getStoreIds().iterator(); iter.hasNext();) {
			StoreId id = iter.next();
			
			if (id.getId().equals(storeId)) {
				iter.remove();
			}
		}
		
		storeRepo.save(store);
	}
	
	/**
	 * 상점ID 활성화
	 * @param storeUid
	 * @param storeId
	 */
	public void activateStoreId(int storeUid, String storeId) {
		
		Store store = getStore(storeUid);
		
		if (store == null) throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");
		
		if (!storeIdRepo.findByIdAndStore(storeId, store).isPresent()) throw new IllegalArgumentException("상점ID를 찾을 수 없습니다.");
		
		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (storeId.equals(id.getId())) {
				id.setActivated(true);
			} else {
				id.setActivated(false);
			}
		}
		
		storeRepo.save(store);
	}
	
	/**
	 * 순간정산 활성화, 성공 시 true 리턴
	 * @param storeUid
	 */
	public boolean instantOn(int storeUid) {
		
		Store store = getStore(storeUid);
		boolean isInstantOn = false;
		
		if (store == null) throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");
		
		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (id.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
				id.setActivated(true);
				isInstantOn = true;
			} else {
				id.setActivated(false);
			}
		}
		
		// 순간정산ID 찾았을 경우에만 저장
		if (isInstantOn) {
			storeRepo.save(store);
		} 
		
		return isInstantOn;
	}
	
	public boolean instantOff(int storeUid) {
		
		Store store = getStore(storeUid);
		boolean isInstantOff = false;
		
		if (store == null) throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");
		
		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (id.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
				id.setActivated(false);
			} else {
				// TODO: Default로 설정된 일반정산ID를 사용하도록 변경해야 함
				// 현재: 처음으로 가져오는 일반정산ID 활성화함
				if (!isInstantOff) {
					id.setActivated(true);
					isInstantOff = true;
				} else {
					id.setActivated(false);
				}
			}
		}
		
		// 순간정산ID 찾았을 경우에만 저장
		if (isInstantOff) {
			storeRepo.save(store);
		} 
		
		return isInstantOff;
	}
	
	public StoreId getStoreId(String id) {
		
		return storeIdRepo.findById(id).orElse(null);
	}
	
	/**
	 * 상점ID 리스트중 정산타입 중복이 있으면 true 리턴
	 * @param store
	 * @return
	 */
	public boolean isStoreIdTypeDuplicated(List<StoreId> ids) {
		
		String typeList = "";
		
		for (StoreId id : ids) {
			if (typeList.indexOf(id.getServiceTypeCode()) < 0) {
				typeList += id.getServiceTypeCode();
			} else {
				return true;
			}
		}
		
		return false;
	}
}
