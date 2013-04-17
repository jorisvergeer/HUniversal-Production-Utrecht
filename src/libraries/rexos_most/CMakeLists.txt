cmake_minimum_required(VERSION 2.8.3)
project(rexos_most)

## Find catkin and any catkin packages
find_package(catkin REQUIRED COMPONENTS roscpp message_generation)

#file(GLOB_RECURSE msgs RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/msg" "*.msg")
#add_message_files(
#DIRECTORY msg
#FILES ${msgs} )
  	
#file(GLOB_RECURSE srvs RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/srv" "*.srv")
#add_service_files(
#DIRECTORY srv
#FILES ${srvs} )

generate_messages (
	DEPENDENCIES std_msgs
)

## Declare a catkin package
catkin_package(
INCLUDE_DIRS include 
LIBRARIES rexos_most 
CATKIN_DEPENDS 
DEPENDS Boost)

file(GLOB_RECURSE tests "test" "*.cpp" "*.c")
catkin_add_gtest(rexos_most_test ${tests})
target_link_libraries(rexos_most_test gtest_main)

file(GLOB_RECURSE sources "src" "*.cpp" "*.c")
include_directories(include ${catkin_INCLUDE_DIRS})
add_library(rexos_most ${sources})

target_link_libraries(rexos_most ${catkin_LIBRARIES})
add_dependencies(rexos_most rexos_most_gencpp)




