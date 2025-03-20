#include "class_encrypter.h"

#include <cstdlib>
#include <cstring>

#include "../includes/xxtea.h"

#define XXTEA_ENCRYPT_START_OFFSET 10
#define XXTEA_SUB_BLOCK_SIZE 4
#define XXTEA_BLOCK_SIZE 8
#define XXTEA_KEY_SIZE 16

#define MAGIC_HEADER_SIZE 4
#define MAGIC_BYTE1 0xCA
#define MAGIC_BYTE2 0xFE
#define MAGIC_BYTE3 0xBA
#define MAGIC_BYTE4 0xBE

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

    xxtea_encrypt_class(class_size, class_data, key_data);
    env->SetByteArrayRegion(class_byte_array, 0, class_size, reinterpret_cast<const jbyte*>(class_data));
    return class_byte_array;
}

void xxtea_encrypt_class(const size_t class_size, unsigned char* class_data, const unsigned char* key_data) {
    for (int i = 0; i < class_size; ++i) {
        class_data[i] = static_cast<char>(convert_to_magic(class_data[i]));
    }

    if (class_size >= MAGIC_HEADER_SIZE) {
        class_data[0] = MAGIC_BYTE1;
        class_data[1] = MAGIC_BYTE2;
        class_data[2] = MAGIC_BYTE3;
        class_data[3] = MAGIC_BYTE4;
    }

    if (class_size > 1 && class_size > MAGIC_HEADER_SIZE) {
        const size_t last_index = class_size - 1;
        const unsigned char temp_data = class_data[MAGIC_HEADER_SIZE];
        class_data[MAGIC_HEADER_SIZE] = class_data[last_index];
        class_data[last_index] = temp_data;
    }

    for (int i = 0; i < (class_size - XXTEA_ENCRYPT_START_OFFSET) / XXTEA_BLOCK_SIZE; i++) {
        xxtea_encrypt_block(XXTEA_ENCRYPT_START_OFFSET + i * XXTEA_BLOCK_SIZE, class_data, key_data);
    }
}

void xxtea_encrypt_block(const int data_offset, unsigned char* class_data, const unsigned char* key_data) {
    unsigned char block1_bytes[XXTEA_SUB_BLOCK_SIZE];
    unsigned char block2_bytes[XXTEA_SUB_BLOCK_SIZE];

    memcpy(block1_bytes, class_data + data_offset, XXTEA_SUB_BLOCK_SIZE);
    memcpy(block2_bytes, class_data + data_offset + XXTEA_SUB_BLOCK_SIZE, XXTEA_SUB_BLOCK_SIZE);

    uint32_t const xxtea_key[4] = {
        (bytes_to_uint32(key_data)),
        (bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE)),
        (bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE * 2)),
        (bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE * 3)),
    };
    uint32_t uintBlocks[2] = {
        bytes_to_uint32(block1_bytes),
        bytes_to_uint32(block2_bytes),
    };

    xxtea_encrypt(uintBlocks, xxtea_key);

    unsigned char encrypted_block1_bytes[XXTEA_SUB_BLOCK_SIZE];
    unsigned char encrypted_block2_bytes[XXTEA_SUB_BLOCK_SIZE];

    uint32_to_bytes(uintBlocks[0], encrypted_block1_bytes);
    uint32_to_bytes(uintBlocks[1], encrypted_block2_bytes);

    memcpy(class_data + data_offset, encrypted_block1_bytes, XXTEA_SUB_BLOCK_SIZE);
    memcpy(class_data + data_offset + XXTEA_SUB_BLOCK_SIZE, encrypted_block2_bytes, XXTEA_SUB_BLOCK_SIZE);
}

unsigned char convert_to_magic(unsigned char byte_value) {
    byte_value -= 2;
    byte_value ^= 0x11;
    byte_value = ~byte_value;
    byte_value += 1;
    byte_value ^= 0x22;
    return byte_value;
}
