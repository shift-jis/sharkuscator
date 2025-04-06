cmake_minimum_required(VERSION 3.30)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_C_STANDARD 11)

project(native_class_encryptor)
enable_language(ASM_MASM)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_library(class_encryptor SHARED
        src/class_encryptor.cpp
        src/class_encryptor.h

        includes/xxtea.cpp
        includes/xxtea.h
)

add_library(class_decryptor SHARED
        src/class_decryptor.cpp
        src/class_decryptor.h

        src/decryptor_agent.cpp
        src/decryptor_agent.h

        includes/xxtea.cpp
        includes/xxtea.h
)
