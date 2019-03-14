package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;

public interface StoreIdRepository extends CrudRepository<StoreId, Integer> {

	public Optional<StoreId> findById(String id);
	public Optional<StoreId> findByIdAndActivated(String id, Boolean activated);
	public Optional<StoreId> findByStoreAndActivated(Store store, Boolean activated);
	public Optional<StoreId> findByIdAndStore(String id, Store store);
}
