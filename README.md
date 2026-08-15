# maturinport — maturin v1.14.1 for z/OS

Port of [maturin](https://github.com/PyO3/maturin) v1.14.1 to IBM z/OS (s390x).

Maturin builds and publishes Rust-backed Python extension modules (PyO3/cffi/uniffi)
as PEP-517 compliant wheels.

## Status

✅ **Working on z/OS** — binary tested on z/OS 2.5 (OS/390 UNIX)

```
$ maturin --version
maturin 1.14.1
```

## Installation

Download the binary from the [latest stable release](../../releases/tag/STABLE_maturinport_1):

```sh
# On z/OS
curl -L https://github.com/zopencommunity/maturinport/releases/download/STABLE_maturinport_1/maturin-1.14.1-zos -o maturin
chmod +x maturin
./maturin --version
```

Or via [zopen package manager](https://github.com/zopencommunity/meta):
```sh
zopen install maturin
```

## Building from Source

Maturin must be cross-compiled from Linux on Power (LoP) targeting `s390x-ibm-zos`.

### Prerequisites

- Linux on Power (ppc64le) with rustc nightly 1.86+
- z/OS server running the cross-compilation Flask server (`cross/server.py`)
- IBM z/OS libc fork: `https://github.ibm.com/compiler/rust-libc`

### Steps

```bash
# Clone maturin
git clone --branch v1.14.1 https://github.com/PyO3/maturin /tmp/maturin-zos
cd /tmp/maturin-zos

# Apply patches
git apply patches/maturin/maturin-1.14.1-zos.patch

# Set up patched dependencies (ring, libc, rustix, etc.) in Cargo.toml [patch.crates-io]
# See patches/ directory for all required crate patches

# Build
cargo build --profile zos --target s390x-ibm-zos --ignore-rust-version \
    --no-default-features --features rustls
```

See [patches/README.md](patches/README.md) for the full list of patches and build infrastructure.

## z/OS Compatibility Notes

- **Edition 2024 let-chains**: Requires nightly `#![feature(let_chains)]` on rustc 1.86
- **Compression**: `bzip2`, `zstd`, `lzma` disabled; uses `deflate` only (no C build infra)
- **Stack switching (psm)**: Disabled; z/OS does not support Linux-style stack-switching asm
- **mlock/munlock**: Stub library `libzos_mlock_stubs.a` provides ENOSYS stubs
- **fdopendir**: Stub library `libzos_fdopendir.a` provides ASCII-compatible implementation
- **TLS**: Uses `rustls` (pure Rust) with `ring` big-endian patches; native-tls disabled
- **Ring**: 6 big-endian fixes for z/OS (Montgomery multiply, P-256, Curve25519, RSA)

## Related Ports

| Port | Package | Version |
|------|---------|---------|
| [uvport](https://github.com/zopencommunity/uvport) | uv | 0.8.13 |
| [pydantic-coreport](https://github.com/zopencommunity/pydantic-coreport) | pydantic-core | 2.41.5 |
| [rpds-pyport](https://github.com/zopencommunity/rpds-pyport) | rpds-py | 2026.6.3 |
| [cryptographyport](https://github.com/zopencommunity/cryptographyport) | cryptography | 50.0.0 |
| [bcryptport](https://github.com/zopencommunity/bcryptport) | bcrypt | 5.0.0 |
| [watchfilesport](https://github.com/zopencommunity/watchfilesport) | watchfiles | 1.2.0 |

## License

maturin is licensed under the [MIT OR Apache-2.0](https://github.com/PyO3/maturin/blob/main/license-mit)
license. This port and its patches are provided under the same terms.
