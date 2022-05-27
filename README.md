# Harvest Tool

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5750537.svg)](https://doi.org/10.5281/zenodo.5750537) [![🤪 Unstable integration & delivery](https://github.com/NASA-PDS/harvest/actions/workflows/unstable-cicd.yaml/badge.svg)](https://github.com/NASA-PDS/harvest/actions/workflows/unstable-cicd.yaml) [![😌 Stable integration & delivery](https://github.com/NASA-PDS/harvest/actions/workflows/stable-cicd.yaml/badge.svg)](https://github.com/NASA-PDS/harvest/actions/workflows/stable-cicd.yaml)

The Harvest Tool captures and indexes product metadata. Each discipline node of the Planetary Data System runs the tool to crawl the local data repositories, discovering products and indexing associated metadata into the Registry Service. As such, it's a sub-component of the PDS Registry App (https://github.com/NASA-PDS/registry).

For more detailed documentation on this tool, see the PDS Registry Documentation: https://nasa-pds.github.io/registry/.


# Documentation

The documentation for the latest release of the Harvest Tool, including release notes, installation, and operation of the software is [ready to browse online](https://nasa-pds.github.io/registry/).

If you would like to get the latest documentation, including any updates since the last release, you can execute the "mvn site:run" command and view the documentation locally at http://localhost:8080/.


## 👥 Contributing

Within the NASA Planetary Data System, we value the health of our community as much as the code. Towards that end, we ask that you read and practice what's described in these documents:

-   Our [contributor's guide](https://github.com/NASA-PDS/.github/blob/main/CONTRIBUTING.md) delineates the kinds of contributions we accept.
-   Our [code of conduct](https://github.com/NASA-PDS/.github/blob/main/CODE_OF_CONDUCT.md) outlines the standards of behavior we practice and expect by everyone who participates with our software.


# Build

The software can be compiled and built with the "mvn compile" command but in order to create the JAR file, you must execute the "mvn compile jar:jar" command.

In order to create a complete distribution package, execute the following commands: 

```
% mvn package
```

# Operational Release

Using [GitHub Actions](https://github.com/features/actions) and the [Roundup](https://github.com/NASA-PDS/roundup-action), releases of this software happen automatically. However, if you need to do so manually, read on.

A release candidate can be created by hand if the community has determined that a release should occur. These steps should be followed when generating a release candidate and when completing the release.

## Clone a Fresh Repository

```
git clone git@github.com:NASA-PDS/harvest.git
```


## Update Version Numbers

Update `pom.xml` for the release version or use the Maven Versions Plugin, e.g.:

```
# Skip this step if this is a RELEASE CANDIDATE, we will deploy as SNAPSHOT version for testing
VERSION=1.15.0
mvn versions:set -DnewVersion=$VERSION
git add pom.xml
```


## Update Changelog

Update Changelog using [Github Changelog Generator](https://github.com/github-changelog-generator/github-changelog-generator). Note: Make sure you set `$CHANGELOG_GITHUB_TOKEN` in your `.bash_profile` or use the `--token` flag.
```
# For RELEASE CANDIDATE, set VERSION to future release version.
github_changelog_generator --future-release v$VERSION

git add CHANGELOG.md
```


## Commit Changes

Commit changes using following template commit message:
```
# For operational release
git commit -m "[RELEASE] harvest v$VERSION"

# For release candidate
CANDIDATE_NUM=1
git commit -m "[RELEASE] harvest v${VERSION}-rc${CANDIDATE_NUM}"

# Push changes to main
git push -u origin main
```


## Build and Deploy Software to [Sonatype Maven Repo](https://repo.maven.apache.org/maven2/gov/nasa/pds/).

```
# For operational release
mvn clean site deploy -P release

# For release candidate
mvn clean site deploy
```

Note: If you have issues with GPG, be sure to make sure you've created your GPG key, sent to server, and have the following in your `~/.m2/settings.xml`:
```
<profiles>
  <profile>
    <activation>
      <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
      <gpg.executable>gpg</gpg.executable>
      <gpg.keyname>KEY_NAME</gpg.keyname>
      <gpg.passphrase>KEY_PASSPHRASE</gpg.passphrase>
    </properties>
  </profile>
</profiles>

```


## Push Tagged Release

```
# For Release Candidate, you may need to delete old SNAPSHOT tag
git push origin :v$VERSION

# Now tag and push
git tag v${VERSION}
git push --tags

```


## Deploy Site to Github Pages

From cloned repo:
```
git checkout gh-pages

# Create specific version site
mkdir -p $VERSION

# Copy the over to version-specific and default sites
rsync -av target/site/ $VERSION
rsync -av $VERSION/* .

git add .

# For operational release
git commit -m "Deploy v$VERSION docs"

# For release candidate
git commit -m "Deploy ${VERSION}-rc${CANDIDATE_NUM} docs"

git push origin gh-pages
```

## Update Versions For Development

Update `pom.xml` with the next SNAPSHOT version either manually or using Github Versions Plugin.

For RELEASE CANDIDATE, ignore this step.

```
git checkout main

# For release candidates, skip to push changes to main
VERSION=1.16.0-SNAPSHOT
mvn versions:set -DnewVersion=$VERSION
git add pom.xml
git commit -m "Update version for $VERSION development"

# Push changes to main
git push -u origin main
```


# Snapshot Release

Deploy software to Sonatype SNAPSHOTS Maven repo:

```
# Operational release
mvn clean site deploy
```



# Maven JAR Dependency Reference

- Operational Releases: https://search.maven.org/search?q=g:gov.nasa.pds%20AND%20a:harvest&core=gav
- Snapshots: https://oss.sonatype.org/content/repositories/snapshots/gov/nasa/pds/harvest/

If you want to access snapshots, add the following to your `~/.m2/settings.xml`:
```xml
<profiles>
  <profile>
     <id>allow-snapshots</id>
     <activation><activeByDefault>true</activeByDefault></activation>
     <repositories>
       <repository>
         <id>snapshots-repo</id>
         <url>https://oss.sonatype.org/content/repositories/snapshots</url>
         <releases><enabled>false</enabled></releases>
         <snapshots><enabled>true</enabled></snapshots>
       </repository>
     </repositories>
   </profile>
</profiles>
```
