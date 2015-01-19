INCLUDE_DIRECTORIES(
   ${MSPP_SOURCE_DIR}/include
   ${MSPP_SOURCE_DIR}/lib/vigra/test/include
   ${MSPP_SOURCE_DIR}/lib/vigra/include
   ${CMAKE_CURRENT_BINARY_DIR}/include
)

#### Sources
SET(SRCS_ALIGNMENT Alignment-test.cpp)
SET(SRCS_ALL all-test.cpp)
SET(SRCS_PEAKLISTCOLLECTION PeakListCollection-test.cpp)
SET(SRCS_STABLEMARRIAGE stablemarriage-test.cpp)

MACRO(ADD_MSPP_TEST name exe src)
    STRING(REGEX REPLACE "test_([^ ]+).*" "\\1" test "${exe}" )

    #build the test
    ADD_EXECUTABLE(${exe} ${src})
    #link the test
    TARGET_LINK_LIBRARIES(${exe} ms++)
    
    #add test to global list of unit test
    ADD_TEST(${name} ${exe})
ENDMACRO(ADD_MSPP_TEST name exe src)

#### Unit tests
ADD_MSPP_TEST("Alignment" test_alignment ${SRCS_ALIGNMENT})
ADD_MSPP_TEST("All" test_all ${SRCS_ALL})
ADD_MSPP_TEST("PeakListCollection" test_peaklistcollection ${SRCS_PEAKLISTCOLLECTION})
ADD_MSPP_TEST("StableMarriage" test_stablemarriage ${SRCS_STABLEMARRIAGE})
