#include "decrypter_agent.h"

#include <sstream>
#include <cstring>
#include <vector>
#include <windows.h>

#include "class_decrypter.h"

unsigned char* KEY_DATA;
char* PACKAGE_NAME;

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {
    char file_path_buffer[256];
    package_separators(file_path_buffer, options);

    unsigned char* package_name = nullptr;
    unsigned char* key_data = nullptr;
    if (!has_package_and_key(file_path_buffer, &package_name, &key_data)) {
        return JNI_ERR;
    }

    PACKAGE_NAME = static_cast<char*>(malloc(strlen(reinterpret_cast<char*>(package_name))));
    strcpy(PACKAGE_NAME, reinterpret_cast<char*>(package_name));

    KEY_DATA = static_cast<unsigned char*>(malloc(16));
    strcpy(reinterpret_cast<char*>(KEY_DATA), reinterpret_cast<char*>(key_data));

    jvmtiEnv* jvmti_env;
    if (const jint return_code = vm->GetEnv(reinterpret_cast<void**>(&jvmti_env), JVMTI_VERSION); JNI_OK != return_code) {
        return return_code;
    }

    jvmtiCapabilities capabilities;
    (void)memset(&capabilities, 0, sizeof(capabilities));
    capabilities.can_generate_all_class_hook_events = 1;
    if (const jvmtiError error = jvmti_env->AddCapabilities(&capabilities); JVMTI_ERROR_NONE != error) {
        return error;
    }

    jvmtiEventCallbacks callbacks;
    (void)memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = &class_decryption_hook;
    if (const jvmtiError error = jvmti_env->SetEventCallbacks(&callbacks, sizeof(callbacks)); JVMTI_ERROR_NONE != error) {
        return error;
    }

    if (const jvmtiError error = jvmti_env->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr); JVMTI_ERROR_NONE != error) {
        return error;
    }

    if (const jint return_code = vm->GetEnv(reinterpret_cast<void**>(&jvmti_env), JVMTI_VERSION_1_0); return_code != JVMTI_ERROR_NONE) {
        return JNI_ERR;
    }

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    // TODO
}

void JNICALL class_decryption_hook(jvmtiEnv* jvmti_env, JNIEnv* jni_env, jclass class_being_redefined, jobject loader, const char* name, jobject protection_domain, jint class_data_len, const unsigned char* class_data, jint* new_class_data_len,
                                   unsigned char** new_class_data) {
    *new_class_data_len = class_data_len;
    jvmti_env->Allocate(class_data_len, new_class_data);

    unsigned char *_data = *new_class_data;
    if (name && strncmp(name, PACKAGE_NAME, strlen(PACKAGE_NAME)) == 0) {
        for (int i = 0; i < class_data_len; i++) {
            _data[i] = class_data[i];
        }
        xxtea_decrypt_class(class_data_len, _data, KEY_DATA);
    } else {
        for (int i = 0; i < class_data_len; i++) {
            _data[i] = class_data[i];
        }
    }
}

void package_separators(char* file_path_buffer, const char* options) {
    strncpy_s(file_path_buffer, 256, options, 256 - 1);
    file_path_buffer[256 - 1] = '\0';

    for (size_t i = 0; file_path_buffer[i] != '\0'; ++i) {
        if (file_path_buffer[i] == '.') {
            file_path_buffer[i] = '/';
        }
    }
}

bool has_package_and_key(const char* options, unsigned char** package_name, unsigned char** key_data) {
    const auto option_values = to_key_value_pairs(options, ',');
    if (option_values.size() < 2) {
        return false;
    }

    const auto package_name_option = to_key_value_pairs(option_values[0], '=');
    if (package_name_option.size() != 2) {
        return false;
    }

    const auto key_data_option = to_key_value_pairs(option_values[1], '=');
    if (key_data_option.size() != 2) {
        return false;
    }

    *package_name = reinterpret_cast<unsigned char*>(strdup(package_name_option[1].c_str()));
    *key_data = reinterpret_cast<unsigned char*>(strdup(key_data_option[1].c_str()));
    return true;
}

std::vector<std::string> to_key_value_pairs(const std::string& input_chars, const char delim) {
    std::stringstream input_stream(input_chars);
    std::vector<std::string> key_value_pairs;
    std::string basic_string;
    while (std::getline(input_stream, basic_string, delim)) {
        key_value_pairs.push_back(basic_string);
    }
    return key_value_pairs;
}
