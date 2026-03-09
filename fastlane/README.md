fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build

```sh
[bundle exec] fastlane android build
```

Build release AAB

### android metadata

```sh
[bundle exec] fastlane android metadata
```

Upload metadata only (no APK/AAB)

### android internal

```sh
[bundle exec] fastlane android internal
```

Deploy to internal test track

### android promote

```sh
[bundle exec] fastlane android promote
```

Promote internal to production

### android production

```sh
[bundle exec] fastlane android production
```

Deploy to production as draft

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
