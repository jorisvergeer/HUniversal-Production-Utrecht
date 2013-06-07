cmake_minimum_required(VERSION 2.8.3)
project(rexos_most)

## Find catkin and any catkin packages
find_package(catkin REQUIRED COMPONENTS roscpp std_msgs genmsg actionlib_msgs actionlib message_generation)
find_package(Mongo)

file(GLOB_RECURSE msgs RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/msg" "*.msg")
add_message_files(
DIRECTORY msg
FILES ${msgs} )
  	
file(GLOB_RECURSE srvs RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/srv" "*.srv")
add_service_files(
DIRECTORY srv
FILES ${srvs} )

file(GLOB_RECURSE actions RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/action" "*.action")
add_action_files(
DIRECTORY action
FILES ${actions} )

generate_messages (
	DEPENDENCIES actionlib_msgs std_msgs
)

## Declare a catkin package
catkin_package(
INCLUDE_DIRS include 
LIBRARIES rexos_most 
CATKIN_DEPENDS 
DEPENDS Boost)

file(GLOB_RECURSE tests "test" "*.cpp" "*.c")
catkin_add_gtest(rexos_most_test ${tests})
target_link_libraries(rexos_most_test ${catkin_LIBRARIES} gtest_main gmock)

file(GLOB_RECURSE sources "src/**.c" "src/**.cpp")
include_directories(include ${catkin_INCLUDE_DIRS} ${MONGO_INCLUDE_DIRS})
message(STATUS "sources: ${sources}")
add_library(rexos_most ${sources})

target_link_libraries(rexos_most ${catkin_LIBRARIES} ${MONGO_LIBRARIES})
add_dependencies(rexos_most rexos_most_gencpp)

add_definitions("-std=c++0x")
