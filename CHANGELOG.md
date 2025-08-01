# Changelog

## [«unknown»](https://github.com/NASA-PDS/harvest/tree/«unknown») (2025-07-29)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.7...«unknown»)

**Defects:**

- Error `FIPS mode: only SunJSSE TrustManagers may be used` [\#247](https://github.com/NASA-PDS/harvest/issues/247) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]
- Harvest failing due to AOSS OCU limits without retrying [\#239](https://github.com/NASA-PDS/harvest/issues/239) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v4.0.7](https://github.com/NASA-PDS/harvest/tree/v4.0.7) (2025-04-21)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.6...v4.0.7)

## [v4.0.6](https://github.com/NASA-PDS/harvest/tree/v4.0.6) (2025-04-03)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.5...v4.0.6)

**Improvements:**

- Disable graceful handling of missing schema configs with `keyword` data type [\#227](https://github.com/NASA-PDS/harvest/issues/227)

**Defects:**

- harvest stops after \[ERROR\] \(HarvestCli.java:runCommand:98\) Object builders can only be used once [\#231](https://github.com/NASA-PDS/harvest/issues/231) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]
- harvest it fails to load org.slf4j.impl.StaticLoggerBinder [\#229](https://github.com/NASA-PDS/harvest/issues/229) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- harvest.log summary not correct [\#149](https://github.com/NASA-PDS/harvest/issues/149) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- harvest.log summary numbers not consistent with number of expected files [\#148](https://github.com/NASA-PDS/harvest/issues/148) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- harvest.log summary does not agree with OpenSearch counts [\#147](https://github.com/NASA-PDS/harvest/issues/147) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]

**Other closed issues:**

- Unsupported index name log message could be more useful [\#233](https://github.com/NASA-PDS/harvest/issues/233)
- Investigate if when multiple harvest run at the same time, there can be an issue with downloading the same LDD in parallel [\#198](https://github.com/NASA-PDS/harvest/issues/198)

## [v4.0.5](https://github.com/NASA-PDS/harvest/tree/v4.0.5) (2024-12-19)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.4...v4.0.5)

**Requirements:**

- As a user, I don't want to insert a product which has been already inserted with the same lidvid [\#195](https://github.com/NASA-PDS/harvest/issues/195)

**Improvements:**

- Quiet Harvest noisy logs for Invalid cookie header [\#223](https://github.com/NASA-PDS/harvest/issues/223)
- Optimize LDD downloads also on errors [\#205](https://github.com/NASA-PDS/harvest/issues/205)
- Add the date time in the log messages [\#203](https://github.com/NASA-PDS/harvest/issues/203)

**Defects:**

- Issue with ALT LDD date format error [\#222](https://github.com/NASA-PDS/harvest/issues/222) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- harvest by default overwrites all products [\#213](https://github.com/NASA-PDS/harvest/issues/213) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]
- Read time out errors occurring with big data uploads [\#208](https://github.com/NASA-PDS/harvest/issues/208) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- OpenSearch mapping conflict issue when trying to change a type \(`[illegal_argument_exception]`\) [\#204](https://github.com/NASA-PDS/harvest/issues/204) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]

## [v4.0.4](https://github.com/NASA-PDS/harvest/tree/v4.0.4) (2024-11-12)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.3...v4.0.4)

**Requirements:**

- As a user, I want harvest to exit with non 0 code when the arguments are not parsable [\#199](https://github.com/NASA-PDS/harvest/issues/199)

**Improvements:**

- Update harvest to support batches with data volumes larger than AOSS allowable limit [\#207](https://github.com/NASA-PDS/harvest/issues/207)

**Defects:**

- Issues identified with uncaught throttling errors [\#206](https://github.com/NASA-PDS/harvest/issues/206) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]
- Unknown date format used that could not be parsed by Harvest [\#197](https://github.com/NASA-PDS/harvest/issues/197) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

**Other closed issues:**

- Double check how the schema is updated, log levels fine tune [\#196](https://github.com/NASA-PDS/harvest/issues/196)

## [v4.0.3](https://github.com/NASA-PDS/harvest/tree/v4.0.3) (2024-10-16)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.2...v4.0.3)

## [v4.0.2](https://github.com/NASA-PDS/harvest/tree/v4.0.2) (2024-10-16)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/ec2...v4.0.2)

## [ec2](https://github.com/NASA-PDS/harvest/tree/ec2) (2024-10-08)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.1...ec2)

**Requirements:**

- As a user, I want to include my organization name in the harvest metadata \(`ops:Harvest_Info.ops:node_name`\) [\#187](https://github.com/NASA-PDS/harvest/issues/187)

**Defects:**

- I want to update the OpenSearch schema whatever the number of fields to be updated [\#190](https://github.com/NASA-PDS/harvest/issues/190) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]
- New records harvested in the registry don't have the expected Node value [\#186](https://github.com/NASA-PDS/harvest/issues/186) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v4.0.1](https://github.com/NASA-PDS/harvest/tree/v4.0.1) (2024-08-27)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v4.0.0...v4.0.1)

## [v4.0.0](https://github.com/NASA-PDS/harvest/tree/v4.0.0) (2024-08-27)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.8.2...v4.0.0)

**Improvements:**

- As a data custodian, I want to load URLs / file paths without unnecessary / additional slashes [\#158](https://github.com/NASA-PDS/harvest/issues/158)

**Defects:**

- ref\_lid\_\* fields are not added to the Registry schema prior to load [\#127](https://github.com/NASA-PDS/harvest/issues/127) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]

**Other closed issues:**

- When harvest lasts more that one hour  AWS credentials need to be renewed [\#172](https://github.com/NASA-PDS/harvest/issues/172)
- Run the synchronization of LDD as needed only [\#159](https://github.com/NASA-PDS/harvest/issues/159) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- Add schema reference to all examples to take advantage of new schema [\#156](https://github.com/NASA-PDS/harvest/issues/156)
- Update to utilize new multi-tenancy approach [\#118](https://github.com/NASA-PDS/harvest/issues/118)

## [v3.8.2](https://github.com/NASA-PDS/harvest/tree/v3.8.2) (2023-12-16)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.8.1...v3.8.2)

**Defects:**

- A bundle that previously loaded throws an error on reload attempt [\#141](https://github.com/NASA-PDS/harvest/issues/141) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v3.8.1](https://github.com/NASA-PDS/harvest/tree/v3.8.1) (2023-11-16)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.8.0...v3.8.1)

**Requirements:**

- As a user, I want to ingest data products with labels having `.lblx` file extension [\#130](https://github.com/NASA-PDS/harvest/issues/130)

**Defects:**

- `Too many requests` error to OpenSearch [\#134](https://github.com/NASA-PDS/harvest/issues/134) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]

**Other closed issues:**

- Improve Fault Tolerance of Harvest for Forbidden Access error and Timeout [\#125](https://github.com/NASA-PDS/harvest/issues/125) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v3.8.0](https://github.com/NASA-PDS/harvest/tree/v3.8.0) (2023-09-28)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.6...v3.8.0)

**Requirements:**

- As a developer, I want to know what version of Harvest was used to load a product [\#119](https://github.com/NASA-PDS/harvest/issues/119)

**Improvements:**

- Improve skipped product INFO message [\#106](https://github.com/NASA-PDS/harvest/issues/106)

**Defects:**

- Access forbidden during nominal pipeline execution of harvest on Mars2020 archive [\#124](https://github.com/NASA-PDS/harvest/issues/124) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]

## [v3.7.6](https://github.com/NASA-PDS/harvest/tree/v3.7.6) (2023-03-30)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.5...v3.7.6)

## [v3.7.5](https://github.com/NASA-PDS/harvest/tree/v3.7.5) (2023-03-30)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.4...v3.7.5)

**Defects:**

- Harvest skips path that is the root of a soft link  [\#102](https://github.com/NASA-PDS/harvest/issues/102) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v3.7.4](https://github.com/NASA-PDS/harvest/tree/v3.7.4) (2022-12-12)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.3...v3.7.4)

**Defects:**

- --overwrite flag is not respected for \<bundles\> elements in harvest config [\#112](https://github.com/NASA-PDS/harvest/issues/112) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v3.7.3](https://github.com/NASA-PDS/harvest/tree/v3.7.3) (2022-11-09)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.2...v3.7.3)

## [v3.7.2](https://github.com/NASA-PDS/harvest/tree/v3.7.2) (2022-10-14)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.0...v3.7.2)

## [v3.7.0](https://github.com/NASA-PDS/harvest/tree/v3.7.0) (2022-09-21)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.6.0...v3.7.0)

**Defects:**

- Incorrect "lidvid" and  "\_id" fields are ingested \(trailing zeros are truncated\) [\#90](https://github.com/NASA-PDS/harvest/issues/90) [[s.critical](https://github.com/NASA-PDS/harvest/labels/s.critical)]

**Other closed issues:**

- Improve CLI flag handling [\#85](https://github.com/NASA-PDS/harvest/issues/85)

## [v3.6.0](https://github.com/NASA-PDS/harvest/tree/v3.6.0) (2022-04-13)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.5.2...v3.6.0)

**Requirements:**

- As a user, I want Harvest automatically convert date / time fields to ISO format supported by Elasticsearch [\#54](https://github.com/NASA-PDS/harvest/issues/54)

**Improvements:**

- Change the option -o help message [\#84](https://github.com/NASA-PDS/harvest/issues/84)

**Defects:**

- Error ingesting an XML boolean with values of 0/1 [\#78](https://github.com/NASA-PDS/harvest/issues/78) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]
- harvest stops rather than skips a file with bad permissions [\#75](https://github.com/NASA-PDS/harvest/issues/75) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

**Other closed issues:**

- Update argument handling to use hyphenation similar to other PDS Tools [\#86](https://github.com/NASA-PDS/harvest/issues/86)

## [v3.5.2](https://github.com/NASA-PDS/harvest/tree/v3.5.2) (2022-01-11)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.5.1...v3.5.2)

## [v3.5.1](https://github.com/NASA-PDS/harvest/tree/v3.5.1) (2021-12-10)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.5.0...v3.5.1)

**Defects:**

- Harvest fails on `yyyyZ` date time [\#70](https://github.com/NASA-PDS/harvest/issues/70) [[s.high](https://github.com/NASA-PDS/harvest/labels/s.high)]

## [v3.5.0](https://github.com/NASA-PDS/harvest/tree/v3.5.0) (2021-09-30)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.4.1...v3.5.0)

**Requirements:**

- As a user, I want to ingest the PDS4 label as JSON in a binary blob form [\#60](https://github.com/NASA-PDS/harvest/issues/60)

**Improvements:**

- Add release datetime to version output [\#64](https://github.com/NASA-PDS/harvest/issues/64)
- Enable blob ingestion by default [\#58](https://github.com/NASA-PDS/harvest/issues/58)
- Quick fix to support date/time conversion to "ISO instant" format [\#55](https://github.com/NASA-PDS/harvest/issues/55)

## [v3.4.1](https://github.com/NASA-PDS/harvest/tree/v3.4.1) (2021-06-30)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.4.0...v3.4.1)

**Improvements:**

- Track collection file inventory  [\#18](https://github.com/NASA-PDS/harvest/issues/18)

**Defects:**

- harvest ingest is not creating all product\_lidvid as an array [\#50](https://github.com/NASA-PDS/harvest/issues/50) [[s.low](https://github.com/NASA-PDS/harvest/labels/s.low)]
- Lid & vid validation, logging enhancement, fix skipped file counter [\#22](https://github.com/NASA-PDS/harvest/issues/22)
- Check input URIs to avoid potential security vulnerability [\#6](https://github.com/NASA-PDS/harvest/issues/6) [[s.low](https://github.com/NASA-PDS/harvest/labels/s.low)]

## [v3.4.0](https://github.com/NASA-PDS/harvest/tree/v3.4.0) (2021-04-16)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.3.3...v3.4.0)

**Requirements:**

- As a user, I want to be able to ingest a directory of data that is not part of a bundle [\#45](https://github.com/NASA-PDS/harvest/issues/45)
- As a user, I want a default configuration for harvest included in the tool package [\#37](https://github.com/NASA-PDS/harvest/issues/37)

## [v3.3.3](https://github.com/NASA-PDS/harvest/tree/v3.3.3) (2021-01-02)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.3.2...v3.3.3)

## [v3.3.2](https://github.com/NASA-PDS/harvest/tree/v3.3.2) (2020-12-21)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.3.1...v3.3.2)

## [v3.3.1](https://github.com/NASA-PDS/harvest/tree/v3.3.1) (2020-12-02)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.2.2...v3.3.1)

**Improvements:**

- Implement date conversion from PDS4 date/time strings to Solr format [\#24](https://github.com/NASA-PDS/harvest/issues/24)
- Update Harvest and Registry documentation to be more concise and streamlined [\#14](https://github.com/NASA-PDS/harvest/issues/14)

**Defects:**

- File system metadata not sufficiently captured per requirements [\#35](https://github.com/NASA-PDS/harvest/issues/35) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- MD5 digest encoding is in Base64 instead of Hex [\#34](https://github.com/NASA-PDS/harvest/issues/34) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- For multivalued fields only unique values are stored [\#30](https://github.com/NASA-PDS/harvest/issues/30)
- Could not parse date in yyyy-MM-ddZ format [\#29](https://github.com/NASA-PDS/harvest/issues/29)
- Fix bug where ingested product start\_date\_time is off by 12 hours [\#4](https://github.com/NASA-PDS/harvest/issues/4)

## [v3.2.2](https://github.com/NASA-PDS/harvest/tree/v3.2.2) (2020-03-28)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.2.1...v3.2.2)

**Defects:**

- Bash script does not launch on macos, likely not on linux  [\#20](https://github.com/NASA-PDS/harvest/issues/20)

## [v3.2.1](https://github.com/NASA-PDS/harvest/tree/v3.2.1) (2020-03-27)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.2.0...v3.2.1)

## [v3.2.0](https://github.com/NASA-PDS/harvest/tree/v3.2.0) (2020-03-26)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.1.0...v3.2.0)

**Improvements:**

- Update Harvest documentation per new scripts and upgrades [\#17](https://github.com/NASA-PDS/harvest/issues/17)

## [v3.1.0](https://github.com/NASA-PDS/harvest/tree/v3.1.0) (2020-03-26)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.0.0...v3.1.0)

## [v3.0.0](https://github.com/NASA-PDS/harvest/tree/v3.0.0) (2020-03-26)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.6.0...v3.0.0)

**Improvements:**

- Improve and simplify Harvest execution and configuration to only manage Registry collection [\#16](https://github.com/NASA-PDS/harvest/issues/16)
- Create new Harvest / Registry package to help streamline the documentation and deployment [\#15](https://github.com/NASA-PDS/harvest/issues/15)

**Defects:**

- Update Harvest to work with upgraded Solr package [\#13](https://github.com/NASA-PDS/harvest/issues/13)

## [v2.6.0](https://github.com/NASA-PDS/harvest/tree/v2.6.0) (2020-01-30)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.5.2...v2.6.0)

**Other closed issues:**

- Open Source Harvest Tool [\#1](https://github.com/NASA-PDS/harvest/issues/1)

## [v2.5.2](https://github.com/NASA-PDS/harvest/tree/v2.5.2) (2019-10-29)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.5.1...v2.5.2)

## [v2.5.1](https://github.com/NASA-PDS/harvest/tree/v2.5.1) (2019-10-27)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.5.0...v2.5.1)

## [v2.5.0](https://github.com/NASA-PDS/harvest/tree/v2.5.0) (2019-10-25)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.4.0...v2.5.0)

## [v2.4.0](https://github.com/NASA-PDS/harvest/tree/v2.4.0) (2019-10-18)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.3.0...v2.4.0)

## [v2.3.0](https://github.com/NASA-PDS/harvest/tree/v2.3.0) (2019-10-18)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v2.2.0...v2.3.0)

## [v2.2.0](https://github.com/NASA-PDS/harvest/tree/v2.2.0) (2019-10-15)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/1f0366f2e342eeef510c2a20a9d7959880203400...v2.2.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
