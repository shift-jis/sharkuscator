#include "class_decrypter.h"

#include "../includes/xxtea.h"
#include <cstdint>
#include <cstring>

#define XXTEA_ENCRYPT_START_OFFSET 10
#define XXTEA_SUB_BLOCK_SIZE 4
#define XXTEA_BLOCK_SIZE 8
#define XXTEA_KEY_SIZE 16

#define MAGIC_HEADER_SIZE 4
#define MAGIC_BYTE1 0xCA
#define MAGIC_BYTE2 0xFE
#define MAGIC_BYTE3 0xBA
#define MAGIC_BYTE4 0xBE

void xxtea_decrypt_class(const jint class_size, unsigned char* class_data, const unsigned char* key_data) {
    if (class_size > XXTEA_ENCRYPT_START_OFFSET) {
        for (size_t i = 0; i < (class_size - XXTEA_ENCRYPT_START_OFFSET) / XXTEA_BLOCK_SIZE; ++i) {
            xxtea_decrypt_block(XXTEA_ENCRYPT_START_OFFSET + i * XXTEA_BLOCK_SIZE, class_data, key_data);
        }
    }

    if (class_size > 1 && class_size > MAGIC_HEADER_SIZE) {
        const size_t last_index = class_size - 1;
        const unsigned char temp_data = class_data[MAGIC_HEADER_SIZE];
        class_data[MAGIC_HEADER_SIZE] = class_data[last_index];
        class_data[last_index] = temp_data;
    }

    for (size_t i = MAGIC_HEADER_SIZE; i < class_size; ++i) {
        class_data[i] = convert_from_magic(class_data[i]);
    }
}

void xxtea_decrypt_block(const int data_offset, unsigned char* class_data, const unsigned char* key_data) {
    unsigned char block1_bytes[XXTEA_SUB_BLOCK_SIZE];
    unsigned char block2_bytes[XXTEA_SUB_BLOCK_SIZE];

    memcpy(block1_bytes, class_data + data_offset, XXTEA_SUB_BLOCK_SIZE);
    memcpy(block2_bytes, class_data + data_offset + XXTEA_SUB_BLOCK_SIZE, XXTEA_SUB_BLOCK_SIZE);

    uint32_t const xxtea_key[4] = {
        bytes_to_uint32(key_data),
        bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE),
        bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE * 2),
        bytes_to_uint32(key_data + XXTEA_SUB_BLOCK_SIZE * 3),
    };
    uint32_t uint_blocks[2] = {
        bytes_to_uint32(block1_bytes),
        bytes_to_uint32(block2_bytes),
    };

    xxtea_decrypt(uint_blocks, xxtea_key);

    unsigned char decrypted_block1_bytes[XXTEA_SUB_BLOCK_SIZE];
    unsigned char decrypted_block2_bytes[XXTEA_SUB_BLOCK_SIZE];

    uint32_to_bytes(uint_blocks[0], decrypted_block1_bytes);
    uint32_to_bytes(uint_blocks[1], decrypted_block2_bytes);

    memcpy(class_data + data_offset, decrypted_block1_bytes, XXTEA_SUB_BLOCK_SIZE);
    memcpy(class_data + data_offset + XXTEA_SUB_BLOCK_SIZE, decrypted_block2_bytes, XXTEA_SUB_BLOCK_SIZE);
}

unsigned char convert_from_magic(unsigned char byte_value) {
    byte_value ^= 0x22;
    byte_value -= 1;
    byte_value = ~byte_value;
    byte_value ^= 0x11;
    byte_value += 2;
    return byte_value;
}
