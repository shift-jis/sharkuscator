#pragma once

#include <jvmti.h>
#include <string>
#include <vector>

void JNICALL class_decryption_hook(jvmtiEnv*, JNIEnv*, jclass, jobject, const char*, jobject, jint, const unsigned char*, jint*, unsigned char**);

void package_separators(char*, const char*);

bool has_package_and_key(const char*, unsigned char**, unsigned char**);

std::vector<std::string> to_key_value_pairs(const std::string&, char);
