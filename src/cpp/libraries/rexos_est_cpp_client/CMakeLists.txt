# ESTStateMachineCppClient #
cmake_minimum_required(VERSION 2.8.3)
project(rexos_est_cpp_client)

## Find catkin and any catkin packages
find_package(catkin REQUIRED)
find_package(Boost)
find_package(Mongo)

## Declare a catkin package
catkin_package(
INCLUDE_DIRS include 
LIBRARIES rexos_state_blackboard_cpp_client 
CATKIN_DEPENDS
DEPENDS Boost Mongo)

file(GLOB_RECURSE sources "src" "*.cpp" "*.c")
include_directories(include ${catkin_INCLUDE_DIRS} ${Boost_INCLUDE_DIRS} ${MONGO_INCLUDE_DIRS})
add_library(rexos_est_cpp_client ${sources})
target_link_libraries(rexos_est_cpp_client ${catkin_LIBRARIES} ${Boost_LIBRARIES} ${MONGO_LIBRARIES})

