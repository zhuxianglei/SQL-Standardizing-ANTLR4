﻿
-- TEST CREATE TABLE
CREATE TABLE T_TEST02/*IPS_SQLCHECK:NO PRIMARY/UNIQUE KEY,NO COMMENT*/
( SN_KEY    INT/*IPS_SQLCHECK:NO COMMENT*/,
  SN VARCHAR(20) COMMENT 'CMT COL SN',
  PN    VARCHAR(20)/*IPS_SQLCHECK:NO COMMENT*/
)