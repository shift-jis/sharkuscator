cmake_minimum_required(VERSION 3.30)
set(CMAKE_CXX_STANDARD 20)

project(sharkuscator_encrypter)
enable_language(ASM)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_library(class_encrypter SHARED
        src/class_encrypter.cpp
        src/class_encrypter.h

        includes/xxtea.cpp
        includes/xxtea.h
)

add_library(class_decrypter SHARED
        src/class_decrypter.cpp
        src/class_decrypter.h

        src/decrypter_agent.cpp
        src/decrypter_agent.h

        includes/xxtea.cpp
        includes/xxtea.h
)
