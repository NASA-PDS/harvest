# Harvest Tool

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5750537.svg)](https://doi.org/10.5281/zenodo.5750537) [[![🤪 Unstable integration & delivery](https://github.com/NASA-PDS/harvest/actions/workflows/unstable-cicd.yaml/badge.svg?branch=main)](https://github.com/NASA-PDS/harvest/actions/workflows/unstable-cicd.yaml) [![😌 Stable integration & delivery](https://github.com/NASA-PDS/harvest/actions/workflows/stable-cicd.yaml/badge.svg)](https://github.com/NASA-PDS/harvest/actions/workflows/stable-cicd.yaml)

The Harvest Tool captures and indexes product metadata. Each discipline node of the Planetary Data System runs the tool to crawl the local data repositories, discovering products and indexing associated metadata into the Registry Service. As such, it's a sub-component of the PDS Registry Application (https://github.com/NASA-PDS/registry).

For more detailed documentation on this tool, see the PDS Registry Documentation: https://nasa-pds.github.io/registry/.


# Documentation

The documentation for the latest release of the Harvest Tool, including release notes, installation, and operation of the software is [ready to browse online](https://nasa-pds.github.io/registry/).

If you would like to get the latest documentation, including any updates since the last release, you can execute the "mvn site:run" command and view the documentation locally at http://localhost:8080/.


## 👥 Contributing

Within the NASA Planetary Data System, we value the health of our community as much as the code. Towards that end, we ask that you read and practice what's described in these documents:

-   Our [contributor's guide](https://github.com/NASA-PDS/.github/blob/main/CONTRIBUTING.md) delineates the kinds of contributions we accept.
-   Our [code of conduct](https://github.com/NASA-PDS/.github/blob/main/CODE_OF_CONDUCT.md) outlines the standards of behavior we practice and expect by everyone who participates with our software.


### 🔢 Versioning

We use the [SemVer](https://semver.org/) philosophy for versioning this software. Or not! Update this as you see fit.


### 🪛 Development

To develop this project, use your favorite text editor, or an integrated development environment with Java support, such as [Eclipse](https://www.eclipse.org/ide/). You'll also need [Apache Maven](https://maven.apache.org/) version 3. With these tools, you can typically run

    mvn package

to produce a complete package. This runs all the phases necessary, including compilation, testing, and package assembly. Other common Maven phases include:

-   `compile` - just compile the source code
-   `test` - just run unit tests
-   `install` - install into your local repository
-   `deploy` - deploy to a remote repository — note that the Roundup action does this automatically for releases

#### :guardsman: Secrets Detection Setup and Update
The PDS uses [Detect Secrets](Detect Secrets](https://nasa-ammos.github.io/slim/docs/guides/software-lifecycle/security/secrets-detection/)) to help prevent committing information to a repository that should remain secret.

For Detect Secrets to work, there is a one-time setup required to your personal global Git configuration, as well as several steps to create or update the **required** `.secrets.baseline` file needed to avoid false positive failures of the software. See [the wiki entry on Detect Secrets](https://github.com/NASA-PDS/nasa-pds.github.io/wiki/Git-and-Github-Guide#detect-secrets) to learn how to do this.

#### 🪝 Pre-Commit Hooks

This package comes with a configuration for [Pre-Commit](https://pre-commit.com/), a system for automating and standardizing `git` hooks for code linting, security scanning, etc. Here in this Java template repository, we use Pre-Commit with [Detect Secrets](https://nasa-ammos.github.io/slim/docs/guides/software-lifecycle/security/secrets-detection/) to prevent the accidental committing or commit messages containing secrets like API keys and passwords.

Pre-Commit and `detect-secrets` are language-neutral, but they themselves are written in Python. To take advantage of these features, you'll need a nearby Python installation. A recommended way to do this is with a virtual Python environment. Using the command line interface, run:

```console
$ python -m venv .venv
$ source .venv/bin/activate   # Use source .venv/bin/activate.csh if you're using a C-style shell
$ pip install pre-commit git+https://github.com/NASA-AMMOS/slim-detect-secrets.git@exp
```

See Detect Secrets information above to setup your secrets baseline prior to proceeding.

Finally, install the pre-commit hooks:

    pre-commit install
    pre-commit install -t pre-push
    pre-commit install -t prepare-commit-msg
    pre-commit install -t commit-msg

You can then work normally. Pre-commit will run automatically during `git commit` and `git push` so long as the Python virtual environment is active.

👉 **Note:** For Detect Secrets to work, there is a one-time setup required to your personal global Git configuration. See [the wiki entry on Detect Secrets](https://github.com/NASA-PDS/nasa-pds.github.io/wiki/Git-and-Github-Guide#detect-secrets) to learn how to do this.


### 🚅 Continuous Integration & Deployment

Thanks to [GitHub Actions](https://github.com/features/actions) and the [Roundup Action](https://github.com/NASA-PDS/roundup-action), this software undergoes continuous integration and deployment. Every time a change is merged into the `main` branch, an "unstable" (known in Java software development circles as a "SNAPSHOT") is created and delivered to [the releases page](https://github.com/NASA-PDS/pds-template-repo-java/releases) and to the [OSSRH](https://central.sonatype.org/publish/publish-guide/).

You can make an official delivery by pushing a `release/X.Y.Z` branch to GitHub, replacing `X` with the major version number, `Y` with the minor version number, and `Z` with the micro version number. This results in a stable (non-SNAPSHOT) release generated and cryptographically signed (but by an automated process so alter trust expectations accordingly) and made available on the releases page and OSSRH; the [website published](https://nasa-pds.github.io/harvest/); changelogs and requirements updated; and a new version number in the `main` branch prepared for future development.

The following sections detail how to do this manually should the automated steps fail.


### 🔧 Manual Publication

**👉 Note:** Requires using [PDS Maven Parent POM](https://github.com/NASA-PDS/pdsen-maven-parent) to ensure release profile is set.


#### Update Version Numbers

Update pom.xml for the release version or use the Maven Versions Plugin, e.g.:
```console
$ # Skip this step if this is a RELEASE CANDIDATE, we will deploy as SNAPSHOT version for testing
$ VERSION=1.15.0
$ mvn -DnewVersion=$VERSION versions:set
$ git add pom.xml
$ git add */pom.xml
```


#### Update Changelog

Update Changelog using [Github Changelog Generator](https://github.com/github-changelog-generator/github-changelog-generator). Note: Make sure you set `$CHANGELOG_GITHUB_TOKEN` in your `.bash_profile` or use the `--token` flag.
```console
$ # For RELEASE CANDIDATE, set VERSION to future release version.
$ GITHUB_ORG=NASA-PDS
$ GITHUB_REPO=validate
$ github_changelog_generator --future-release v$VERSION --user $GITHUB_ORG --project $GITHUB_REPO --configure-sections '{"improvements":{"prefix":"**Improvements:**","labels":["Epic"]},"defects":{"prefix":"**Defects:**","labels":["bug"]},"deprecations":{"prefix":"**Deprecations:**","labels":["deprecation"]}}' --no-pull-requests --token $GITHUB_TOKEN
$ git add CHANGELOG.md
```


#### Commit Changes

Commit changes using following template commit message:
```console
$ # For operational release
$ git commit -m "[RELEASE] Validate v$VERSION"
$ # Push changes to main
$ git push --set-upstream origin main
```


#### Build and Deploy Software to Maven Central Repo
```console
$ # For operational release
$ mvn --activate-profiles release clean site site:stage package deploy
$ # For release candidate
$ mvn clean site site:stage package deploy
```


#### Push Tagged Release
```console
$ # For Release Candidate, you may need to delete old SNAPSHOT tag
$ git push origin :v$VERSION
$ # Now tag and push
$ REPO=validate
$ git tag v${VERSION} -m "[RELEASE] $REPO v$VERSION" -m "See [CHANGELOG](https://github.com/NASA-PDS/$REPO/blob/main/CHANGELOG.md) for more details."
$ git push --tags

```


#### Deploy Site to Github Pages

From cloned repo:
```console
$ git checkout gh-pages
$ # Copy the over to version-specific and default sites
$ rsync --archive --verbose target/staging/ .
$ git add .
$ # For operational release
$ git commit -m "Deploy v$VERSION docs"
$ # For release candidate
$ git commit -m "Deploy v${VERSION}-SNAPSHOT docs"
$ git push origin gh-pages
```


#### Update Versions For Development

Update `pom.xml` with the next SNAPSHOT version either manually or using Github Versions Plugin.

For RELEASE CANDIDATE, ignore this step.
```console
$ git checkout main
$ # For release candidates, skip to push changes to main
$ VERSION=1.16.0-SNAPSHOT
$ mvn -DnewVersion=$VERSION versions:set
$ git add pom.xml
$ git commit -m "Update version for $VERSION development"
$ # Push changes to main
$ git push --set-upstream origin main
```


#### Complete Release in Github

Currently the process to create more formal release notes and attach Assets is done manually through the Github UI.

*NOTE: Be sure to add the `tar.gz` and `zip` from the `target/` directory to the release assets, and use the CHANGELOG generated above to create the RELEASE NOTES.*


## 📃 License

The project is licensed under the [Apache version 2](LICENSE.md) license.



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
