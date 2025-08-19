<h1 align="center">Sharkuscator</h1>
<p align="center">
  A simple yet powerful obfuscator for Java and Kotlin, built to explore the capabilities of Java ASM.
</p>
<p align="center">
  <img alt="Language" src="https://img.shields.io/badge/language-Kotlin-7F52FF.svg?style=flat-square">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square">
</p>

## About The Project

Sharkuscator is an obfuscation tool designed to make Java and Kotlin bytecode harder to read and reverse-engineer. This
project was started as a way to dive deep into the Java ASM library and understand the intricacies of bytecode
manipulation. It provides a variety of transformation techniques to protect your code.

## 📦 Project Setup

### 1. Clone this repository

```bash
git clone https://github.com/shift-jis/sharkuscator.git
```

### 2. Build the Project

```bash
./gradlew build
```

## ⚙️ Usage

To run Sharkuscator, you will need to provide an input JAR, an output path, and a configuration file.

```bash
java -jar sharkuscator-version.jar --input <input.jar> --output <output.jar> --config <configuration.json>
```

## 🔧 Configuration Details

- `slash_class_entries`: When set to `true`, slashes (/) are inserted into obfuscated class names, making them appear as
  if they are in a directory structure.
- `exclusions`: In addition to the global `exclusions` at the root of the configuration, you can also define exclusions
  within each individual transformer. This allows for more granular control over which classes or packages are skipped
  by a specific transformation.
- The exclusion patterns use a glob-like syntax. The `**` wildcard is automatically replaced with `.*` to function as a
  regular expression that matches any character sequence. For example, `com/example/**` will exclude all classes under
  the com.example package and its sub-packages.

## 🔗 Credits & References

Some parts of this project were inspired by or based on the following open-source projects:

- [skidfuscator-java-obfuscator](https://github.com/skidfuscatordev/skidfuscator-java-obfuscator)
- [code-encryptor](https://github.com/4ra1n/code-encryptor)
- [radon](https://github.com/ItzSomebody/radon)

Special thanks to the authors of these tools for sharing their work.

## 📄 License

This project is licensed under the MIT License.
