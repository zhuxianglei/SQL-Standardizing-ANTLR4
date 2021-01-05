
-- TEST CREATE TABLE
CREATE TABLE T_TEST02/*IPS_SQLCHECK:NO COMMENT*/
( SN_KEY    INT/*IPS_SQLCHECK:NO COMMENT*/,
  SN VARCHAR(20) COMMENT 'CMT COL SN',
  PN    VARCHAR(20)/*IPS_SQLCHECK:NO COMMENT*/,
  MD    VARCHAR(20)/*IPS_SQLCHECK:NO COMMENT*/,
  DESCP    VARCHAR(20)/*IPS_SQLCHECK:NO COMMENT*/,
  INDEX IDX_TEST02_SN (SN),
  KEY IDX_TEST02_PN (PN),
  CONSTRAINT UNIQUE KEY UK_TEST02_MD (MD) 
);

-- TEST ERROR SYNTAX
CREATE TABLE TEST03
( SN_KEY INT,
  SN VARCHAR(20),
  PN VARCHAR(20) COMMENT 'COL PN',
  MD VARCHAR(20),
  DESCP VARCHAR(20),
);

-- TEST PARTITION TABLE 
CREATE TABLE T_TABLE01/*IPS_SQLCHECK:NO COMMENT*/ (
    id INT PRIMARY KEY COMMENT 'PK',
    YEAR_COL    INT/*IPS_SQLCHECK:NO COMMENT*/
)
PARTITION BY RANGE (year_col) (
    PARTITION PT_0 VALUES LESS THAN (1991),
    PARTITION PT_1 VALUES LESS THAN (1995),
    PARTITION PT_2 VALUES LESS THAN (1999),
    PARTITION PT_3 VALUES LESS THAN (2003)
);

-- TEST ADD COLUMNS
ALTER TABLE t1 ADD COLUMN (C2    INT/*IPS_SQLCHECK:NO COMMENT*/,C3    VARCHAR(10)/*IPS_SQLCHECK:NO COMMENT*/),ALGORITHM=INPLACE,LOCK=NONE;
-- 测试增加partition
ALTER TABLE t1 ADD PARTITION (PARTITION PT_3 VALUES LESS THAN (2002)),ALGORITHM=INPLACE,LOCK=NONE;
-- 测试索引和约束
ALTER TABLE t2 ADD INDEX IDX_T2_D  (d), ADD UNIQUE  UK_T2_A (a),ALGORITHM=INPLACE,LOCK=NONE;
ALTER TABLE TEST13 ADD CONSTRAINT FK_TEST13_C_10 FOREIGN KEY(C_10) REFERENCES TEST02(SN_KEY),ALGORITHM=INPLACE,LOCK=NONE;
ALTER TABLE TEST13 ADD UNIQUE INDEX UK_TEST13_C (C),ALGORITHM=INPLACE,LOCK=NONE;
ALTER TABLE TEST13 add PRIMARY KEY(A),ALGORITHM=INPLACE,LOCK=NONE;
-- 测试创建proc function
delimiter //
CREATE PROCEDURE SP_SIMPLEPROC (OUT param1 INT)
BEGIN
    SELECT COUNT(*) INTO param1 FROM t;
END;//
delimiter ;

delimiter $$
CREATE FUNCTION `F_SHORTEN`(S VARCHAR(255), N INT)
    RETURNS varchar(255)
BEGIN
   IF ISNULL(S) THEN
      RETURN '';
   END IF;
END;$$
delimiter ;