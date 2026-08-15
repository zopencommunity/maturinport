# maturinport Patches

This directory contains patches required to cross-compile maturin v1.14.1
for z/OS (s390x-ibm-zos) from a Linux on Power (LoP) machine.

## Patch Overview

### maturin/ — maturin v1.14.1 source patches
- `maturin-1.14.1-zos.patch` — Source-level fixes:
  - `src/lib.rs` / `src/main.rs`: Add `#![feature(let_chains)]` for edition 2024 let-chain support on nightly rustc 1.86
  - `src/compression.rs`: Map `Bzip2`/`Zstd` compression methods to `Deflated` (native C libs removed for z/OS)
  - `Cargo.toml`: Remove `bzip2`, `zstd`, `lzma` features from `zip` dep (no C build infra on z/OS cross)

### ring/ — ring v0.17.14 cryptography patches
Big-endian z/OS fixes (6 root causes). See [ring-0.17.14-zos.patch](ring/ring-0.17.14-zos.patch).

### libc/ — IBM z/OS libc fork
Complete IBM fork of rust-libc with full z/OS support:
- Source: `https://github.ibm.com/compiler/rust-libc` branch `zOS.0.2.169`
- Provides `target_os = "zos"` types, constants, function signatures

### rustix/ — rustix v1.1.4
- `src/backend/libc/fs/types.rs`: Guard `FALLOC_FL_*` constants (Linux-only) behind `not(target_os = "zos")`
- `src/backend/libc/io/errno.rs`: Guard Linux-specific errno codes (EBADE, EBADR, EL2*, etc.)
- `src/backend/libc/fs/makedev.rs`: Wrap `makedev` call in `unsafe` block
- `src/backend/libc/fs/dir.rs`: z/OS `dirent` struct field differences
- `src/ioctl/mod.rs`: z/OS ioctl compatibility
- `src/termios/types.rs`: z/OS termios differences

### errno/ — errno v0.3.14
- `src/unix.rs`: Add `#[cfg_attr(target_os = "zos", link_name = "__errno")]`

### getrandom-02/ — getrandom v0.2.17
- `src/util_libc.rs`: Add z/OS to `use_file` backend

### getrandom-03/ — getrandom v0.3.4 (indirect dep)
- `src/backends.rs`: Add `zos` to `use_file` backend selection

### getrandom-04/ — getrandom v0.4.2
- `src/backends.rs`: Add `zos` to `use_file` backend selection
- `src/utils/get_errno.rs`: z/OS errno function name
- `Cargo.toml`: Add `libc` dependency for z/OS

### nix/ — nix v0.31.2
- `src/sys/signal.rs`: Remove unsupported z/OS signals (SIGPWR, SIGWINCH, etc.)
- `src/errno.rs`: z/OS errno codes

### target-lexicon/ — target-lexicon v0.13.5
- `src/targets.rs`: Add `Zos` OS variant and `s390x-ibm-zos` triple

### jobserver/ — jobserver v0.1.34
- `src/unix.rs`: Guard `pthread_kill` behind `#[cfg(not(target_os = "zos"))]`

### filetime/ — filetime v0.2.27
- `src/unix/utimes.rs`: Fix `tv_usec_pad` field, remove unsupported `bitrig` cfg

### zeroize/ — zeroize v1.8.2
- `src/barrier.rs`: z/OS memory barrier compatibility

### time/ — time v0.3.55
- Various: nightly feature gates for let-chains in const fn context

### ar_archive_writer/ — ar_archive_writer v0.5.3
- Fix `let`-chains and `is_multiple_of` usage for rustc 1.86

### ignore/ — ignore v0.4.33
- `src/incremental.rs`: Rewrite let-chains to nested ifs (edition 2021 compat)
- `Cargo.toml`: Remove `rust-version = "1.88"` constraint

### platform-info/ — platform-info v2.1.0
- `src/platform/unix.rs`: Add `target_os = "zos"` to domainname exclusion list (z/OS `utsname` has no `domainname` field)

### zip/ — zip v8.6.0
- `src/datetime.rs`: Rewrite let-chains, replace `is_multiple_of` with `%`
- `src/read.rs`: Rewrite let-chains
- `src/write.rs`: Rewrite let-chains

### psm/ — psm v0.1.32
- `build.rs`: Return `None` for z/OS (no stack-switching asm available)

### lddtree/ — lddtree v0.5.1
- `src/lib.rs`: Rewrite 4 let-chains to nested ifs
- `src/macho.rs`: Rewrite 1 let-chain

### icu_collections/ — icu_collections v2.3.0
- `src/codepointinvliststringlist/mod.rs`: Rewrite 3 let-chains
- `src/codepointinvlist/utils.rs`: Replace `is_multiple_of` with `%`

### icu_locale_core/ — icu_locale_core v2.3.0
- `src/data.rs`, `src/langid.rs`, `src/locale.rs`: Rewrite let-chains
- `src/extensions/other/mod.rs`: Fix `inherent_str_constructors` usage
- `src/parser/langid.rs`, `src/extensions/transform/mod.rs`: Rewrite let-chains

### icu_provider/ — icu_provider v2.3.0
- `src/request.rs`: Replace `str::from_utf8_unchecked` with `core::str::from_utf8_unchecked`

### icu_normalizer/ — icu_normalizer v2.3.0
- `src/properties.rs`: Rewrite 2 let-chains to nested ifs

## Build Infrastructure

- **libc**: IBM fork at `https://github.ibm.com/compiler/rust-libc`
- **ring**: Patched source at `/tmp/ring-zos`
- **mlock stubs**: `libzos_mlock_stubs.a` injected by `server.py`
- **fdopendir stub**: `libzos_fdopendir.a` injected by `server.py`
- **Proc-macro wrapper**: `rustc-wrapper-maturin.sh` injects `.so` files for 9 proc-macros
- **Edition**: maturin uses edition 2024; `#![feature(let_chains)]` added for nightly compatibility
