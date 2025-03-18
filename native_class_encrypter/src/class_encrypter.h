#pragma once

#include <jni.h>

extern "C" {
JNIEXPORT jbyteArray JNICALL Java_dev_sharkuscator_obfuscator_encryption_ClassEncrypter_encrypt(JNIEnv*, jclass, jbyteArray, jbyteArray);
}

unsigned char* encrypt_class(jsize, unsigned char*, unsigned char*);

unsigned char convert_to_magic(unsigned char);
