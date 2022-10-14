# Changelog

## [release/3.7.1](https://github.com/NASA-PDS/harvest/tree/release/3.7.1) (2022-10-14)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.7.0...release/3.7.1)

## [v3.7.0](https://github.com/NASA-PDS/harvest/tree/v3.7.0) (2022-09-21)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/release/3.7.0...v3.7.0)

## [release/3.7.0](https://github.com/NASA-PDS/harvest/tree/release/3.7.0) (2022-09-06)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.6.0...release/3.7.0)

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

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/3.3.0...v3.3.1)

**Defects:**

- File system metadata not sufficiently captured per requirements [\#35](https://github.com/NASA-PDS/harvest/issues/35) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]
- MD5 digest encoding is in Base64 instead of Hex [\#34](https://github.com/NASA-PDS/harvest/issues/34) [[s.medium](https://github.com/NASA-PDS/harvest/labels/s.medium)]

## [3.3.0](https://github.com/NASA-PDS/harvest/tree/3.3.0) (2020-10-14)

[Full Changelog](https://github.com/NASA-PDS/harvest/compare/v3.2.2...3.3.0)

**Improvements:**

- Implement date conversion from PDS4 date/time strings to Solr format [\#24](https://github.com/NASA-PDS/harvest/issues/24)
- Update Harvest and Registry documentation to be more concise and streamlined [\#14](https://github.com/NASA-PDS/harvest/issues/14)

**Defects:**

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
