# Tiktok-Q

Q-T is a mod for TikTok on Android. `build.sh` takes an official TikTok APK, patches it, and produces a signed APK with the mod included.




## Features

- Broken "Liquid Glass" theme
- Incomplete / half-baked AMOLED theme 
- Showcase for Badges and Gradients 
- Completely stripped automatic requests to the original server (including crash log reporting) 
- Skip login and birthday prompts
- Display user region and language
- Blocked yzewe.ru
- Basic plugin system safeguards with automated scanning for sketchy junk in .dex files
- Improved Developer Mode

## Requirements

- Python 3.7 or newer
- A JDK (`java` and `keytool` must be on your PATH)
- An official universal TikTok APK (type APK, not BUNDLE), arm64-v8a, nodpi (v47.2.41 was used in dev)

The TikTok APK is not included in this repository.

## Build

```sh
./build.sh build path/to/tiktok.apk
```

This builds the mod and signs it with the debug key. The output goes to `build/`.

Other commands:

```sh
./build.sh build tiktok.apk --unsigned            # build only: build/soufit-unsigned.apk
./build.sh build-sign tiktok.apk --keystore release.p12
./build.sh sign build/soufit-unsigned.apk --keystore release.p12
./build.sh verify build/soufit.apk                # check that an APK is signed
./build.sh tools hashes                           # print SHA-256 of each cached tool
```

## License

* **Core License:** GPL-3.0-only — see [LICENSE](LICENSE).
* **Upstream Terms:** Subject to additional terms from the original project — see [ADDITIONAL-TERMS.md](https://git.narez.xyz/narezany/MargyT/src/branch/main/ADDITIONAL-TERMS.md).
