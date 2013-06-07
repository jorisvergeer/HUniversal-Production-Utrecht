cmake_minimum_required(VERSION 2.8.3)
project(equiplet_scada)

## Find catkin and any catkin packages
find_package(catkin REQUIRED COMPONENTS roscpp std_msgs rexos_most rexos_std_msgs rexos_std_srvs rexos_blackboard_cpp_client libjson)
find_package(Mongo)

## Declare a catkin package
catkin_package(
LIBRARIES  
CATKIN_DEPENDS roscpp std_msgs rexos_most rexos_std_msgs rexos_std_srvs rexos_blackboard_cpp_client libjson
DEPENDS)

file(GLOB_RECURSE sources "src/**.c" "src/**.cpp")
include_directories(${catkin_INCLUDE_DIRS})
add_executable(${PROJECT_NAME} ${sources})

target_link_libraries(${PROJECT_NAME} ${catkin_LIBRARIES})

add_definitions("-std=c++0x")
add_definitions("-DNO_SSL")

