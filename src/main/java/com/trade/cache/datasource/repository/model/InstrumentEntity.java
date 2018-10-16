package com.trade.cache.datasource.repository.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="TRADED_INSTRUMENTS", schema = "myschema")
public class InstrumentEntity {
    @EmbeddedId
    private TradedInstrumentId tradedInstrumentId;

    private BigDecimal price;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    private InstrumentEntity() {
    }

    public InstrumentEntity(String vendorId, String symbol, BigDecimal price) {
        this.tradedInstrumentId = new TradedInstrumentId(vendorId, symbol);
        this.price = price;
        this.lastUpdate = Timestamp.valueOf(LocalDateTime.now());
    }

    public TradedInstrumentId getTradedInstrumentId() {
        return tradedInstrumentId;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public InstrumentEntity withPrice(BigDecimal price) {
        this.price = price;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentEntity that = (InstrumentEntity) o;
        return Objects.equals(tradedInstrumentId, that.tradedInstrumentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradedInstrumentId);
    }

    @Override
    public String toString() {
        return "InstrumentEntity{" +
                "tradedInstrumentId=" + tradedInstrumentId +
                ", price=" + price +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
