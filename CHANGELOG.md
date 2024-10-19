# Changelog

## 0.32.1

Fixed
- Neoforge event registration

## 0.32.0

Added
- Networking registrator, to support cross-loader packet and handler registration

## 0.31.0

Added
- Included [kotlin-events](https://github.com/svby/kotlin-events)
- Custom event utilities

## 0.30.0

Major change
- Neoforge support! Tentative, will need further testing
- Rename to FilloaxLib (except package names for sanity)

## 0.29.0

Added
- Simple networking API

Internal
- Switch from Minivan to ModDevGradle, actually works when using common module outside

## 0.28.1

Internal
- Switch from VanillaGradle to Minivan, should speed up first/github build

## 0.28 [1.21]

Changed
- Update to 1.21, which led to next changes:
- Change constructor and builder for ForcePosJigsawStructure

Removed:
- Remove FxSinglePoolElement as its main purpose was selectively disabling waterlogging
and you can do that in vanilla SinglePoolElements now.

---

[Previous versions not changelogged]