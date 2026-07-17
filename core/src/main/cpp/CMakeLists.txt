cmake_minimum_required(VERSION 3.22.1)
project("QmBlur")

add_library(
        QmBlur
        SHARED
        BlurNative.c
)

find_library(
        log-lib
        log
)

find_library(
        jni-graphics-lib
        jnigraphics
)

target_link_libraries(
        QmBlur
        ${log-lib}
        ${jni-graphics-lib}
)

target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")

# AGP builds non-debuggable variants as RelWithDebInfo (-O2); the blur inner
# loops are the hottest code in the library, so opt into full optimization.
target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE "-O3")