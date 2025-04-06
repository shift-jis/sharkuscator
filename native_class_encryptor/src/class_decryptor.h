#pragma once

#include <jni.h>

void xxtea_decrypt_class(jint, unsigned char*, const unsigned char*);

void xxtea_decrypt_block(int, unsigned char*, const unsigned char*);

unsigned char convert_from_magic(unsigned char);
