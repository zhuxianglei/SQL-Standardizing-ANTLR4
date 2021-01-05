﻿-- 11 table1
-- 12 table1测试
CREATE TABLE `BENE`.`TEST01`
( `SN_KEY` INT NOT NULL,
  `SN` VARCHAR(20) NOT NULL COMMENT 'SERIAL NO国家',
  `PN` VARCHAR(20),
  `MD` VARCHAR(20),
  `DESCP` VARCHAR(20),
  INDEX `IDX_SN` (`SN`),
  KEY `IDX_PN`(`PN`,`DESCP`)
);
CREATE TABLE `TEST02`
( `SN_KEY` INT NOT NULL,
  `SN` VARCHAR(20) NOT NULL,
  `PN` VARCHAR(20) COMMENT 'PN COMMENT',
  `MD` VARCHAR(20),
  `DESCP` VARCHAR(20),
  INDEX `IDX_SN` (`SN`),
  KEY `IDX_PN`(`PN`,`DESCP`),
);

-- 31 table3测试
-- 32 table3测试
CREATE TABLE TEST03
( SN3_KEY INT NOT NULL PRIMARY KEY,
  SN3 VARCHAR(20) NOT NULL,
  PN3 VARCHAR(20),
  MD3 VARCHAR(20)  COMMENT 'MD3 COMMENT国际',
  DESCP3 VARCHAR(20),
  INDEX IDX_SN3 (SN3),
  KEY IDX_PN3(PN3)
);

ALTER TABLE XLZHU_TEST04 CHANGE SN SN2 VARCHAR(41);
ALTER TABLE `TEST05` ADD CONSTRAINT CC_SN_KEY PRIMARY KEY (SN_KEY) COMMENT 'INDEX SN_KEY';