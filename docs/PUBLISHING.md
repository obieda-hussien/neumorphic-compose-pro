# Publishing 5.0.0

JitPack remains available; no signing configuration is required to compile.
Central publishing uses the current Publisher Portal, not the retired OSSRH endpoint.

1. Verify ownership of `io.github.obieda-hussien` in https://central.sonatype.com/.
2. Add repository secrets `SIGNING_KEY` (armored private PGP key) and `SIGNING_PASSWORD`.
3. Run **Stage Central release** on the exact validated commit. It creates signed AAR,
   POM, sources and Dokka documentation artifacts for both modules, checks expected
   signatures and packages checksum files into `central-release-bundle`.
4. Download the bundle and upload through https://central.sonatype.com/publishing.
   Review validation and publish only after device verification. This workflow does
   not automatically make an irreversible public release.

For local staging:

```sh
bash gradlew :library:publishReleasePublicationToCentralStagingRepository :library-views:publishReleasePublicationToCentralStagingRepository
python3 scripts/central-bundle.py build/central-staging build/central-bundle.zip
```

Credentials are read from environment variables; never put private keys in the repository.
The unsigned local staging tasks are useful for inspection, but the bundle script rejects
unsigned artifacts. Central availability must be checked after publication before changing
installation examples to Maven Central coordinates.

Official reference: https://central.sonatype.org/publish/publish-portal-upload/
