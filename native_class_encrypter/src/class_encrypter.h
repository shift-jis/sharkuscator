#pragma once

#include <jni.h>

extern "C" {
JNIEXPORT jbyteArray JNICALL Java_dev_sharkuscator_obfuscator_encryption_ClassEncrypter_encrypt(JNIEnv*, jclass, jbyteArray, jbyteArray);
}

void xxtea_encrypt_class(size_t, unsigned char*, const unsigned char*);

void xxtea_encrypt_block(int, unsigned char*, const unsigned char*);

unsigned char convert_to_magic(unsigned char);
