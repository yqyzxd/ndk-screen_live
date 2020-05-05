cmake_minimum_required(VERSION 3.4.1)
#不使用ssl
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
aux_source_directory(. SOURCE)

add_library(
       rtmp
        STATIC
        ${SOURCE} )