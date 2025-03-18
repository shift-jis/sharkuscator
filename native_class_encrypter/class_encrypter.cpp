#include "class_encrypter.h"

#include <cstdlib>
#include <cstring>

JNIEXPORT auto Java_dev_sharkuscator_obfuscator_encryption_ClassEncrypter_encrypt(JNIEnv* env, jclass cls, jbyteArray class_byte_array, jbyteArray key_byte_array) -> jbyteArray {
    const jsize class_size = env->GetArrayLength(class_byte_array);
    const auto class_data = static_cast<unsigned char*>(malloc(class_size));
    if (class_data == nullptr) {
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Failed to allocate memory for class data");
        return nullptr;
    }

    const auto class_elements = env->GetByteArrayElements(class_byte_array, nullptr);
    if (class_elements == nullptr) {
        free(class_data);
        return nullptr;
    }
    memcpy(class_data, class_elements, class_size);

    const auto key_data = static_cast<unsigned char*>(malloc(16));
    if (key_data == nullptr) {
        free(class_data);
        env->ReleaseByteArrayElements(class_byte_array, class_elements, JNI_ABORT);
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Failed to allocate memory for key data");
        return nullptr;
    }

    const auto key_elements = env->GetByteArrayElements(key_byte_array, nullptr);
    if (key_elements == nullptr) {
        env->ReleaseByteArrayElements(class_byte_array, class_elements, JNI_ABORT);
        free(class_data);
        free(key_data);
        return nullptr;
    }
    memcpy(key_data, key_elements, 16);

    const auto encrypted_class = encrypt_class(class_size, class_data, key_data);
    env->SetByteArrayRegion(class_byte_array, 0, class_size, reinterpret_cast<const jbyte*>(encrypted_class));

    env->ReleaseByteArrayElements(class_byte_array, class_elements, JNI_OK);
    env->ReleaseByteArrayElements(key_byte_array, key_elements, JNI_ABORT);

    free(class_data);
    free(key_data);

    return class_byte_array;
}

unsigned char* encrypt_class(const jsize class_size, unsigned char* class_data, unsigned char* key_data) {
    for (int i = 0; i < class_size; ++i) {
        class_data[i] = static_cast<char>(convert_to_magic(class_data[i]));
    }

    if (class_size >= 4) {
        class_data[0] = static_cast<char>(0xCA);
        class_data[1] = static_cast<char>(0xFE);
        class_data[2] = static_cast<char>(0xBA);
        class_data[3] = static_cast<char>(0xBE);
    }

    if (class_size > 1) {
        const size_t last_index = class_size - 1;
        const auto temp_data = class_data[4];
        if (last_index >= 4) {
            class_data[4] = class_data[last_index];
            class_data[last_index] = temp_data;
        }
    }

    return class_data;
}

unsigned char convert_to_magic(unsigned char byte_value) {
    byte_value -= 2;
    byte_value ^= 0x11;
    byte_value = ~byte_value;
    byte_value += 1;
    byte_value ^= 0x22;
    return byte_value;
}
