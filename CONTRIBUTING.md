# Contributing to VIK

## Releasing

A release is a git tag: `git tag v0.1.0 && git push origin v0.1.0`. The CI's `publish` job then signs
and uploads the six library modules (JVM, Scala.js and Scala Native rows) to Maven Central under
`net.bovel`, with the tag's version (sbt-ci-release; the version between tags is a snapshot and is
never published by the CI).

One-time setup, done once by the owner:

1. A [Central Portal](https://central.sonatype.com) account with the `net.bovel` namespace verified
   (a DNS TXT record on `bovel.net`), and a user token (Account → Generate User Token).
2. A signing key: `gpg --gen-key`, then publish the public key
   (`gpg --keyserver keyserver.ubuntu.com --send-keys <id>`).
3. The four repository secrets:
   `gh secret set SONATYPE_USERNAME` and `SONATYPE_PASSWORD` (the user token's two halves),
   `gh secret set PGP_SECRET` (the armored key, base64 in one line:
   `gpg --armor --export-secret-keys <id> | base64 -w0`), and `PGP_PASSPHRASE`.

## Guidelines

- KISS: Keep it short and simple.
- Commit messages start with an imperative verb. Short and clear. First line less than 60 chars. Markdown welcome. Example: prefer ``Move `Simplifier` to `vik` `` to `The simplifier joins the solver`. See [How to write a git commit message](https://cbea.ms/git-commit/).
