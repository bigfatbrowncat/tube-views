cmake_minimum_required (VERSION 2.8)

# Title
set(PROJNAME "tube-views")
project (${PROJNAME})

# Native code

#	N1. Titles
#set(LIBNAME ${PROJNAME})
#find_package(JNI)

#	N2. Options and flags
#if(MINGW)
#	SET(CMAKE_SHARED_LIBRARY_PREFIX "")
#	SET(CMAKE_SHARED_MODULE_PREFIX "")
#endif (MINGW)

#SET(CMAKE_C_FLAGS "-std=gnu99")

#IF(APPLE)
#	SET(MACOSX_RPATH FALSE)
#    SET(EXTRA_LIBS "")
#	SET(CMAKE_EXE_LINKER_FLAGS "-std=gnu99")
#ELSEIF(WIN32)
#	SET(EXTRA_LIBS "")
#	SET(CMAKE_EXE_LINKER_FLAGS "-std=gnu99 -static -static-libgcc -static-libstdc++ -mwindows")
#	SET(CMAKE_SHARED_LINKER_FLAGS "-std=gnu99 -static -static-libgcc -static-libstdc++ -mwindows")
#ENDIF()

#	N3. Sources
#file(GLOB SOURCES_CPP "${CMAKE_CURRENT_SOURCE_DIR}/src/native/*.cpp")
#set(SOURCES
#	${SOURCES_CPP} 
#)

#	N4. Includes

#include_directories(
#	${JAVA_INCLUDE_PATH}
#	${JAVA_INCLUDE_PATH2}
#)

#	N5. Linking
#add_library(
#	${LIBNAME}
#	SHARED 
#	${SOURCES}
#)
#target_link_libraries(${LIBNAME})

# Java code

# 	J1. Titles
set(JARNAME "${PROJNAME}-java")
find_package(Java REQUIRED)
include(cmake_patches/UseJavaCustom.cmake)

# 	J2. Compiler options
set(CMAKE_JAVA_COMPILE_FLAGS "-source" "1.7" "-target" "1.7")
set(CMAKE_JAVA_INCLUDE_PATH 
	"${CMAKE_BINARY_DIR}/firststep/firststep-java.jar" 
	"${CMAKE_BINARY_DIR}/bellmodel/bellmodel-java.jar"
	"${CMAKE_BINARY_DIR}/org.json.jar"
)

# 	J3. Sources and java resources
set(CMAKE_CURRENT_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/src/java")
file(GLOB_RECURSE SOURCES_JAVA "*.java")
file(GLOB_RECURSE RESOURCES_TTF  RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "${CMAKE_CURRENT_SOURCE_DIR}/*.ttf")
file(GLOB_RECURSE RESOURCES_PNG  RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "${CMAKE_CURRENT_SOURCE_DIR}/*.png")
file(GLOB_RECURSE RESOURCES_JSON RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "${CMAKE_CURRENT_SOURCE_DIR}/*.json")

# 	J4. Linking
add_jar(${JARNAME} ${SOURCES_JAVA} ${RESOURCES_TTF} ${RESOURCES_PNG} ${RESOURCES_JSON})

#	J5 (optional). Printing out the paths
get_target_property(_jarFile ${JARNAME} JAR_FILE)
get_target_property(_classDir ${JARNAME} CLASSDIR)
message(STATUS "[${PROJNAME}] Jar file: ${_jarFile}")
message(STATUS "[${PROJNAME}] Classes compiled to: ${_classDir}")

# Custom tasks

if (MINGW)
	set(
		CMD "${CMAKE_CURRENT_SOURCE_DIR}/../../run-mingw.sh"
	)
elseif (APPLE)
	set(
		CMD "${CMAKE_CURRENT_SOURCE_DIR}/../../run-osx.sh"
	)
else ()
	set(
		CMD "${CMAKE_CURRENT_SOURCE_DIR}/../../run-unix.sh"
	)
endif ()

message(STATUS "Runner command: ${CMD}")

add_custom_target(
	run 
	COMMAND ${CMD} ${Java_JAVA_EXECUTABLE}
)

file(GLOB jars "${CMAKE_SOURCE_DIR}/lib/*.jar")

add_custom_target(copy_jars ALL)
foreach(jar ${jars})
  add_custom_command(TARGET copy_jars PRE_BUILD
                     COMMAND ${CMAKE_COMMAND} -E
                         copy ${jar} ${CMAKE_BINARY_DIR})
endforeach()

# Dependencies

#	D1. Subprojects (with flags)
add_subdirectory(firststep)
add_subdirectory(bellmodel)

#	D2. Target dependencies
add_custom_target(${PROJNAME} DEPENDS ${JARNAME} copy_jars firststep bellmodel)
add_dependencies(run ${PROJNAME})

#add_dependencies(${PROJNAME} ${JARNAME})


