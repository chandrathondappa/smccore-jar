SET DATABASE SQL SYNTAX DB2 TRUE;
CREATE SCHEMA CORP AUTHORIZATION DBA;

CREATE TABLE CORP.FVEHFIL (
	VFUNIT                        CHAR(10)            Default ' ',
	VFCORP                        CHAR(4)             Default ' ',
	VFDIST                        CHAR(4)             Default ' ',
	VFDISN                        CHAR(24)            Default ' ',
	VFLOCN                        CHAR(2)             Default ' ',
	VFREGN                        CHAR(4)             Default ' ',
	VFADMN                        CHAR(4)             Default ' ',
	VFOWNR                        CHAR(4)             Default ' ',
	VFALTN                        CHAR(4)             Default ' ',
	VFCUST                        CHAR(6)             Default ' ',
	VFBGRP                        CHAR(2)             Default ' ',
	VFCUSN                        CHAR(28)            Default ' ',
	VFUSE                         CHAR(6)             Default ' ',
	VFLTYP                        CHAR(8)             Default ' ',
	VFCLIS                        CHAR(2)             Default ' ',
	VFCLIN                        CHAR(10)            Default ' ',
	VFCLIX                        DECIMAL(7,0)        Default 0,
	VFPLIS                        CHAR(2)             Default ' ',
	VFPLIN                        CHAR(10)            Default ' ',
	VFPLIX                        DECIMAL(7,0)        Default 0,
	VFVIN                         CHAR(20)            Default ' ',
	VFVIN5                        CHAR(5)             Default ' ',
	VFVCST                        CHAR(8)             Default ' ',
	VFSPST                        CHAR(8)             Default ' ',
	VFSCOM                        CHAR(24)            Default ' ',
	VFCAT                         CHAR(8)             Default ' ',
	VFSIZE                        CHAR(8)             Default ' ',
	VFTYPE                        CHAR(8)             Default ' ',
	VFSLPC                        CHAR(1)             Default ' ',
	VFDESC                        CHAR(24)            Default ' ',
	VFBTYP                        CHAR(8)             Default ' ',
	VFAXLS                        DECIMAL(1,0)        Default 0,
	VFDRAX                        DECIMAL(1,0)        Default 0,
	VFACTG                        DECIMAL(3,0)        Default 0,
	VFMYR                         DECIMAL(3,0)        Default 0,
	VFMFRC                        CHAR(3)             Default ' ',
	VFMAKE                        CHAR(12)            Default ' ',
	VFMODL                        CHAR(8)             Default ' ',
	VFCOLR                        CHAR(8)             Default ' ',
	VFGVW                         DECIMAL(7,0)        Default 0,
	VFTARE                        DECIMAL(7,0)        Default 0,
	VFHIFT                        DECIMAL(3,0)        Default 0,
	VFHIIN                        DECIMAL(3,0)        Default 0,
	VFWIFT                        DECIMAL(3,0)        Default 0,
	VFWIIN                        DECIMAL(3,0)        Default 0,
	VFLGFT                        DECIMAL(3,0)        Default 0,
	VFLGIN                        DECIMAL(3,0)        Default 0,
	VFLLTH                        DECIMAL(3,0)        Default 0,
	VFTSIZ                        CHAR(10)            Default ' ',
	VFTIRT                        CHAR(6)             Default ' ',
	VFBASE                        DECIMAL(3,0)        Default 0,
	VFKEYC                        CHAR(12)            Default ' ',
	VFTANK                        DECIMAL(3,0)        Default 0,
	VFNTNK                        DECIMAL(1,0)        Default 0,
	VFFUEL                        CHAR(8)             Default ' ',
	VFNEW                         CHAR(4)             Default ' ',
	VFFHUT                        CHAR(1)             Default ' ',
	VFTAXR                        CHAR(1)             Default ' ',
	VFREEF                        CHAR(1)             Default ' ',
	VFLIFT                        CHAR(1)             Default ' ',
	VFENDT                        DECIMAL(7,0)        Default 0,
	VFTRDT                        DECIMAL(7,0)        Default 0,
	VFINDT                        DECIMAL(7,0)        Default 0,
	VFSBDT                        DECIMAL(7,0)        Default 0,
	VFSBMI                        DECIMAL(7,0)        Default 0,
	VFTTDT                        DECIMAL(7,0)        Default 0,
	VFOSDT                        DECIMAL(7,0)        Default 0,
	VFTSDT                        DECIMAL(7,0)        Default 0,
	VFSLDT                        DECIMAL(7,0)        Default 0,
	VFLDDT                        DECIMAL(7,0)        Default 0,
	VFLBDT                        DECIMAL(7,0)        Default 0,
	VFLMDT                        DECIMAL(7,0)        Default 0,
	VFCMIL                        DECIMAL(7,0)        Default 0,
	VFCMDT                        DECIMAL(7,0)        Default 0,
	VFCWIP                        DECIMAL(9,2)        Default 0,
	VFBKRT                        DECIMAL(9,2)        Default 0,
	VFBKV                         DECIMAL(9,2)        Default 0,
	VFTRES                        DECIMAL(9,2)        Default 0,
	VFDMOS                        DECIMAL(3,0)        Default 0,
	VFEDDT                        DECIMAL(7,0)        Default 0,
	VFAMTL                        DECIMAL(7,2)        Default 0,
	VFAMRT                        DECIMAL(7,2)        Default 0,
	VFAMVL                        DECIMAL(7,2)        Default 0,
	VFAMOS                        DECIMAL(3,0)        Default 0,
	VFAEDT                        DECIMAL(7,0)        Default 0,
	VFPOTL                        DECIMAL(9,2)        Default 0,
	VFTCAP                        DECIMAL(9,2)        Default 0,
	VFGRBA                        DECIMAL(7,0)        Default 0,
	VFOCST                        DECIMAL(9,2)        Default 0,
	VFTCST                        DECIMAL(9,2)        Default 0,
	VFOSTX                        DECIMAL(9,2)        Default 0,
	VFTSTX                        DECIMAL(9,2)        Default 0,
	VFLMOS                        DECIMAL(3,0)        Default 0,
	VFLMIL                        DECIMAL(7,0)        Default 0,
	VFLREV                        DECIMAL(7,0)        Default 0,
	VFLDEP                        DECIMAL(7,0)        Default 0,
	VFLMNT                        DECIMAL(7,0)        Default 0,
	VFLWAR                        DECIMAL(7,0)        Default 0,
	VFLTIR                        DECIMAL(7,0)        Default 0,
	VFFITP                        CHAR(8)             Default ' ',
	VFFIST                        CHAR(8)             Default ' ',
	VFTXCD                        CHAR(3)             Default ' ',
	VFTXNR                        CHAR(8)             Default ' ',
	VFTXTP                        CHAR(3)             Default ' ',
	VFTXLA                        DECIMAL(9,2)        Default 0,
	VFTXRT                        DECIMAL(9,2)        Default 0,
	VFTXLM                        DECIMAL(3,0)        Default 0,
	VFTXLP                        DECIMAL(7,0)        Default 0,
	VFTXBO                        DECIMAL(9,2)        Default 0,
	VFTXRP                        DECIMAL(7,0)        Default 0,
	VFNTPN                        CHAR(8)             Default ' ',
	VFUTPN                        CHAR(8)             Default ' ',
	VFTIST                        CHAR(2)             Default ' ',
	VFTINB                        CHAR(8)             Default ' ',
	VFMSOS                        CHAR(8)             Default ' ',
	VFSLNM                        CHAR(28)            Default ' ',
	VFSLA1                        CHAR(28)            Default ' ',
	VFSLA2                        CHAR(28)            Default ' ',
	VFSLCT                        CHAR(24)            Default ' ',
	VFSLST                        CHAR(2)             Default ' ',
	VFSLZP                        CHAR(9)             Default ' ',
	VFSLTP                        CHAR(8)             Default ' ',
	VFSLRS                        CHAR(8)             Default ' ',
	VFSLPR                        DECIMAL(9,2)        Default 0,
	VF1A01                        CHAR(1)             Default ' ',
	VF2A01                        CHAR(1)             Default ' ',
	VF3A08                        CHAR(8)             Default ' ',
	VF4A08                        CHAR(8)             Default ' ',
	VF5N70                        DECIMAL(7,0)        Default 0,
	VF6N70                        DECIMAL(7,0)        Default 0,
	VF7N72                        DECIMAL(7,2)        Default 0,
	VF8N72                        DECIMAL(7,2)        Default 0,
	VF9N72                        DECIMAL(7,2)        Default 0);

CREATE TABLE CORP.VEHCMPSGF
(
   COMPONENT_ID decimal(9,0) PRIMARY KEY NOT NULL,
   COMPONENT_GROUP char(15) NOT NULL,
   SUB_GROUP char(15) NOT NULL,
   SUB_COMPONENT_NAME char(15) NOT NULL,
   COMPONENT_GROUP_ID bigint NOT NULL,
   DISPLAY_SEQUENCE decimal(4,0) NOT NULL,
   COMPONENT_TYPE char(1) NOT NULL,
   UNIT_OF_MEASUREMENT char(9) NOT NULL,
   VALIDATION_PROGRAM char(10) NOT NULL,
   PARAMETER_LIST char(2) NOT NULL,
   COMPONENT_FILE_FIELD char(10) NOT NULL,
   INDICATOR_VEH_INQ bigint NOT NULL,
   ENTER_DATE date NOT NULL,
   ENTER_TIME time NOT NULL,
   ENTERED_BY char(10) NOT NULL,
   LAST_CHG_DATE date NOT NULL,
   LAST_CHG_TIME time NOT NULL,
   LAST_CHG_BY char(10) NOT NULL,
   EXTRA_3ALPH char(3) NOT NULL,
   EXTRA_6ALPH char(6) NOT NULL,
   EXTRA_ALPHA char(10) NOT NULL
);

CREATE TABLE CORP.VEHCMPSG2F
(
   COMPONENT_ID decimal(9,0) PRIMARY KEY NOT NULL,
   FIELD_LENGTH bigint NOT NULL,
   DECIMAL_POSITIONS bigint NOT NULL,
   HALF_ADJUST char(1) NOT NULL,
   ENTER_DATE date NOT NULL,
   ENTER_TIME time NOT NULL,
   ENTERED_BY char(10) NOT NULL,
   LAST_CHG_DATE date NOT NULL,
   LAST_CHG_TIME time NOT NULL,
   LAST_CHG_BY char(10) NOT NULL
);

CREATE TABLE CORP.VEHCMPF
(
   VEHICLE_NUMBER char(10) NOT NULL,
   OWNING_CORP char(4) NOT NULL,
   S_COMPONENT_ID decimal(9,0) NOT NULL,
   COMPONENT_TEXT char(30) NOT NULL,
   VALUE_15_0 decimal(15,4) NOT NULL,
   UPDATED_DATE date NOT NULL,
   UPDATED_TIME time NOT NULL,
   UPDATED_BY char(10) NOT NULL,
   ENTERED_DATE date NOT NULL,
   ENTERED_TIME time NOT NULL,
   ENTERED_BY char(10) NOT NULL,
   EXTRA_6ALPH char(6) NOT NULL,
   EXTRA_8ALPH char(8) NOT NULL,
   EXTRA_10_ALPHA char(10) NOT NULL
);