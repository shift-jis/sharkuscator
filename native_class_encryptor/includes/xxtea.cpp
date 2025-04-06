#include "xxtea.h"

#include <cstdint>

void xxtea_encrypt(uint32_t* value, const uint32_t* key) {
    uint32_t v0 = value[0], v1 = value[1], sum = 0;
    for (uint32_t i = 0; i < 32; i++) {
        sum += 0x9e3779b9;
        v0 += (v1 << 4) + key[0] ^ (v1 + sum) ^ (v1 >> 5) + key[1];
        v1 += (v0 << 4) + key[2] ^ (v0 + sum) ^ (v0 >> 5) + key[3];
    }
    value[0] = v0;
    value[1] = v1;
}

void xxtea_decrypt(uint32_t* value, const uint32_t* key) {
    uint32_t v0 = value[0], v1 = value[1], sum = 0xC6EF3720;
    for (uint32_t i = 0; i < 32; i++) {
        v1 -= (v0 << 4) + key[2] ^ (v0 + sum) ^ (v0 >> 5) + key[3];
        v0 -= (v1 << 4) + key[0] ^ (v1 + sum) ^ (v1 >> 5) + key[1];
        sum -= 0x9e3779b9;
    }
    value[0] = v0;
    value[1] = v1;
}

uint32_t bytes_to_uint32(const uint8_t* bytes) {
    uint32_t value = 0;
    for (size_t i = 0; i < 4; i++) {
        value |= static_cast<uint32_t>(bytes[i]) << 8 * i;
    }
    return value;
}

void uint32_to_bytes(const unsigned int num, unsigned char * bytes) {
    for (size_t i = 0; i < 4; i++) {
        bytes[i] = static_cast<uint8_t>((num >> (8 * i)) & 0xFF);
    }
}
