<h1 align="center">Sharkuscator</h1>
<p align="center">A simple obfuscator written in kotlin</p>

## 📦 Project Setup
### 1. Clone this repository
```bash
git clone https://github.com/RabiesDev/sharkuscator.git
```

### 2. Build the Project
```bash
./gradlew build
```

## 📚 Required Libraries
Before building, make sure to place the following external libraries into the ./thirdparty directory:

* [MapleIR 1.0.0](https://github.com/LLVM-but-worse/maple-ir/releases/tag/1.0.0-SNAPSHOT-1) (Required)
* [Native Obfuscator 3.5.4r](https://github.com/radioegor146/native-obfuscator/releases/tag/3.5.4r) (Optional)

## ⚙️ Configuration Example
Below is an example configuration.json:

```json
{
    "transformers": {
        "local_variable_rename": {
            "enabled": false,
            "dictionary": "cjk_unified_ideograph",
            "prefix": ""
        },
        "resource_rename": {
            "enabled": false,
            "prefix": ""
        },
        "method_rename": {
            "enabled": false,
            "dictionary": "cjk_unified_ideograph",
            "prefix": ""
        },
        "class_rename": {
            "enabled": false,
            "dictionary": "cjk_unified_ideograph",
            "prefix": ""
        },
        "field_rename": {
            "enabled": false,
            "dictionary": "cjk_unified_ideograph",
            "prefix": ""
        },
        "reflect_rename": {
            "enabled": false
        },

        "local_variable_remove": {
            "enabled": false
        },
        "source_stripper": {
            "enabled": false
        },

        "long_constant_encryption": {
            "enabled": false
        },
        "string_encryption": {
            "enabled": false
        },
        "number_complexity": {
            "enabled": false
        },
        "synthetic_access": {
            "enabled": false
        },
        "dynamic_invoke": {
            "enabled": false
        },
        "native_obfuscate": {
            "enabled": false
        }
    },
    "libraries": [
    ],
    "exclusions": [
        "oshi.*",
        "org.*",
        "com.*"
    ]
}
```

## 🔗 Credits & References
Some parts of this project were inspired by or based on the following open-source projects:

* [skidfuscator-java-obfuscator](https://github.com/skidfuscatordev/skidfuscator-java-obfuscator)
* [code-encryptor](https://github.com/4ra1n/code-encryptor)
* [radon](https://github.com/ItzSomebody/radon)

Special thanks to the authors of these tools for sharing their work.

## 📄 License
This project is licensed under the MIT License.
