# 1Password CLI Java Wrapper [![CI](https://github.com/mpdeimos/onepassword-java/workflows/CI/badge.svg)](https://github.com/mpdeimos/onepassword-java/actions)

## Features

The primary use case for the wrapper is provisioning of users, groups and vaults.
Support for editing or reading passwords is not implemented yet, however, pull requests are welcome.

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
