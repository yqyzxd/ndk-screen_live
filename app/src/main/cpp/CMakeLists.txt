cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(librtmp)
include_directories(librtmp)
add_library(
             native-lib
             SHARED
             com_wind_ndk_screen_live_Rtmp.cpp )



find_library(
        log-lib
              log )


target_link_libraries(
                       native-lib
                        rtmp
                       ${log-lib} )