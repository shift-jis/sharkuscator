#pragma once

#include <cstdint>

void xxtea_encrypt(uint32_t*, const uint32_t*);

void xxtea_decrypt(uint32_t*, const uint32_t*);

uint32_t bytes_to_uint32(const uint8_t*);

void uint32_to_bytes(unsigned int, unsigned char *);
