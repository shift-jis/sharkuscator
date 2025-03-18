#include "class_decrypter.h"

unsigned char* decrypt(const jsize class_size, unsigned char* class_data) {
    if (class_size > 1) {
        const size_t last_index = class_size - 1;
        const auto temp_data = class_data[4];
        if (last_index >= 4) {
            class_data[4] = class_data[last_index];
            class_data[last_index] = temp_data;
        }
    }

    for (int i = 4; i < class_size; ++i) {
        class_data[i] = static_cast<char>(convert_from_magic(class_data[i]));
    }

    return class_data;
}

unsigned char convert_from_magic(unsigned char byte_value) {
    byte_value ^= 0x22;
    byte_value -= 1;
    byte_value = ~byte_value;
    byte_value ^= 0x11;
    byte_value += 2;
    return byte_value;
}
