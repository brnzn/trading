package com.trade.cache.datasource.repository;

import com.trade.cache.datasource.repository.model.InstrumentEntity;
import com.trade.cache.datasource.repository.model.TradedInstrumentId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InstrumentsRepository extends CrudRepository<InstrumentEntity, TradedInstrumentId> {

    List<InstrumentEntity> findByTradedInstrumentIdSymbol(String symbol);

    List<InstrumentEntity> findByTradedInstrumentIdVendorId(String vendorId);

    @Query("select t from InstrumentEntity t where t.lastUpdate <= :lastUpdate")
    List<InstrumentEntity> findAllWithLastUpdateDateTimeBefore(@Param("lastUpdate") Date creationDateTime);
}
