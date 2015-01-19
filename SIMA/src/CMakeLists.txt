SET(SRCS 
    Alignment.cpp
    DistanceMatrix.cpp
    MahalanobisDistance.cpp
    Peak.cpp
    PeakList.cpp
    PeakListBase.cpp
    PeakListCollection.cpp	
    PeakListPrivate.cpp
    StableMarriage.cpp
)

#for MSVC to define a "Header Files" subfolder:
SOURCE_GROUP("Header Files" FILES mspp_header)

ADD_LIBRARY(ms++ ${SRCS} ${mspp_header})
