<h1 align="center">Sharkuscator</h1>
<p align="center">A simple obfuscator written in kotlin</p>

## Setup workspace

1. Clone this repository
2. Run `./gradlew build`

## Placing Required Libraries

This project requires some external libraries to be placed in the `thirdparty` folder.  
After downloading the libraries, make sure to place them in the `./thirdparty` directory.

* [MapleIR (1.0.0)](https://github.com/LLVM-but-worse/maple-ir/releases/tag/1.0.0-SNAPSHOT-1)
* [Native Obfuscator(3.5.4r)](https://github.com/radioegor146/native-obfuscator/releases/tag/3.5.4r)

## Configuration

Here is a sample configuration

```json
{
    "transformers": {
        "resource_rename": {
            "enabled": false,
            "prefix": ""
        },
        "method_rename": {
            "enabled": true,
            "prefix": ""
        },
        "class_rename": {
            "enabled": true,
            "prefix": ""
        },
        "field_rename": {
            "enabled": true,
            "prefix": ""
        },

        "local_variable_remove": {
            "enabled": true
        },
        "source_stripper": {
            "enabled": true
        },

        "synthetic_access": {
            "enabled": true
        },
        "native_obfuscate": {
            "enabled": true
        }
    },
    "exclusions": [
        "oshi.*",
        "org.*",
        "com.*"
    ]
}
```
