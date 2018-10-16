create table MYSCHEMA.TRADED_INSTRUMENTS
(
   VENDOR_ID VARCHAR(10) not null,
   SYMBOL VARCHAR(10) not null,
   PRICE DECIMAL not null,
   LAST_UPDATE TIMESTAMP not null,
   primary key(VENDOR_ID, SYMBOL)
);