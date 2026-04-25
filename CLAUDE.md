# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Harvest is a NASA PDS (Planetary Data System) tool that crawls local data repositories to discover PDS4 products and index their metadata into the Registry Service (Elasticsearch-based). It's a sub-component of the PDS Registry Application.

**Documentation:** https://nasa-pds.github.io/registry/

## Build Commands

```bash
# Build and package (produces JAR, ZIP, tar.gz)
mvn package

# Compile only
mvn compile

# Run tests
mvn test

# Run a single test
mvn test -Dtest=TestClassName

# Install to local repository
mvn install

# View documentation locally
mvn site:run  # then visit http://localhost:8080/
```

**Requirements:** Java 11, Maven 3.x

## Running Harvest

```bash
# Basic usage
harvest -c <config-file>

# With options
harvest -c config.xml -o <output-dir> -l <log-file> -v DEBUG
harvest -c config.xml -O  # overwrite existing products
harvest -c config.xml -a staged  # set archive status
```

## Architecture

### Entry Point Flow
`HarvestMain` → `HarvestCli` (CLI parsing) → `HarvestCmd` (orchestration)

### Key Packages

- **`cfg/`** - JAXB-generated configuration classes from XSD schema. `ConfigManager` handles XML parsing.
- **`cmd/`** - CLI commands. `HarvestCmd` is the main executor that routes to processors.
- **`crawler/`** - File system crawling and product processing:
  - `BaseProcessor` - Abstract base with metadata extraction pipeline
  - `BundleProcessor`, `CollectionProcessor`, `ProductProcessor` - PDS4 type-specific processors
  - `FilesProcessor` - Processes file manifests
- **`dao/`** - Registry (Elasticsearch) interaction:
  - `RegistryManager` - Singleton managing ES connection
  - `RegistryDao`, `MetadataWriter` - Data access layer
  - `RegistryDocBatch` - Batch document writes (default 50)
- **`meta/`** - Metadata extraction via XPath (`XPathExtractor`, `XPathCacheManager`)
- **`util/`** - Logging (`log/`), output writing (`out/`), XML utilities (`xml/`)

### Metadata Extraction Pipeline (in BaseProcessor)
1. `BasicMetadataExtractor` - LID, VID, title, product class
2. `AutogenExtractor` - Auto-discover fields from labels
3. `FileMetadataExtractor` - File information & checksums
4. `InternalReferenceExtractor` - Reference relationships
5. `SearchMetadataExtractor` - Full-text search fields
6. `XPathExtractor` - Custom XPath expressions
7. `MissingFieldsProcessor` - Handle missing/default fields
8. `MetadataNormalizer` - Normalize dates/booleans

### Configuration
XML-based configuration validated against `/src/main/resources/conf/configuration.xsd`. Example configs in `/src/main/resources/conf/examples/`:
- `bundles.xml` - Bundle harvesting
- `directories.xml` - Directory crawling
- `files.xml` - File manifest processing
- `xpaths.xml` - Custom XPath field extraction

### Key Dependencies
- `registry-common` - NASA PDS shared library for Registry interaction
- Saxon-HE - XML/XPath processing
- Apache Tika - File type detection
- JAXB (Jakarta) - XML binding

## Pre-commit Setup

Uses detect-secrets for security scanning:
```bash
python -m venv .venv
source .venv/bin/activate
pip install pre-commit git+https://github.com/NASA-AMMOS/slim-detect-secrets.git@exp
pre-commit install
pre-commit install -t pre-push
pre-commit install -t prepare-commit-msg
pre-commit install -t commit-msg
```

## CI/CD

- **unstable-cicd.yaml** - Builds SNAPSHOT on main branch push
- **stable-cicd.yaml** - Official releases via `release/X.Y.Z` branch
- Releases published to Maven Central and GitHub Releases
