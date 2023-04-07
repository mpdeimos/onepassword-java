# 1Password CLI Java Wrapper [![CI](https://github.com/mpdeimos/onepassword-java/workflows/CI/badge.svg)](https://github.com/mpdeimos/onepassword-java/actions)

---

This branch is compatible with version 2 of the CLI only and currently work in progress.
The stable version, which is compatible with CLI version 1 is on [branch v1](https://github.com/mpdeimos/onepassword-java/tree/v1).

---

## Features

This wrapper was mostly written for automatic provisioning of users, groups and vaults, hence, support for editing or reading passwords is not implemented yet.
This functionality will be added with the upgrade to version 2 of the CLI.

## Usage

The API is straight forward to use and mostly aligns with the 1password CLI, giving it a Java-ish touch.
After creating a `OnePassword` object, you can access entities and their sub-commands like this:

```java
try (OnePassword op = new OnePassword("https://my-account.1password.com", "me@myaccount.com", "A3-xxx", () -> getMyPasswordSecurely())) {
	User[] users = op.users().list();
}

```

Please note, the constructor forces you to pass the account password as delegate.
The reason is that you should be reminded to e.g. retrieve and decode the credentials only when needed and not store these in plain text.

## Developing

* Import the repository as Gradle project in your IDE (VSCode, IntelliJ, Eclipse).
* If tests complain about that `build/bin/op` cannot be found, execute `./gradlew bootstrap` first. This will download the 1password CLI (at the moment only implemented for Linux and Windows).
