# maturinport — maturin v1.14.1 for z/OS

Port of [maturin](https://github.com/PyO3/maturin) v1.14.1 to IBM z/OS (s390x).

Maturin builds and publishes Rust-backed Python extension modules (PyO3/cffi/uniffi)
as PEP-517 compliant wheels.

## Status

The binary runs on z/OS 2.5 and reports its version:

```
$ maturin --version
maturin 1.14.1
```

**It cannot build anything yet.** Every subcommand that does real work needs
cargo and rustc, and neither exists natively on z/OS — the Rust toolchain for
this platform is a cross-compiler that runs on Linux:

```
$ maturin build
💥 maturin failed
  Caused by: Cargo metadata failed. Do you have cargo in your PATH?

$ maturin list-python
💥 maturin failed
  Caused by: rustc, the rust compiler, is not installed or not in PATH.
```

`sdist` is not an exception despite "without compiling" in its description; it
still reads cargo metadata to decide what to package.

So what works today is `--version` and `--help` for each subcommand. Installing
maturin does not let you build a Rust-backed wheel on z/OS, and nothing can
until a native cargo exists. Wheels for this platform are cross-compiled on
Linux, which is how pydantic-core, rpds-py and uv itself are produced.

## Why the wheel still matters

The port publishes an installable wheel as well as the binary, because a binary
on PATH unblocks nothing. Packages built with maturin declare

```toml
[build-system]
requires = ["maturin>=1.9,<2"]
build-backend = "maturin"
```

so pip must install a Python package named `maturin` into its build environment
and import it as a PEP 517 backend. It never consults PATH. Without the wheel,
pip goes to PyPI, finds nothing for this platform, tries to build maturin's
sdist, and that needs Rust — so the build dies before the package's own code is
reached:

```
Collecting maturin<2,>=1.13.3
  Downloading maturin-1.14.1.tar.gz
  Preparing metadata (pyproject.toml): error
```

With the wheel in the index, that step succeeds and the build proceeds to the
point where cargo is genuinely needed:

```
Installing build dependencies: finished with status 'done'
Preparing metadata (pyproject.toml): error
  Checking for Rust toolchain.... Rust not found
```

Still a failure, but an honest one that names the real obstacle — and it is the
piece that has to exist before a native cargo would be sufficient on its own.

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
