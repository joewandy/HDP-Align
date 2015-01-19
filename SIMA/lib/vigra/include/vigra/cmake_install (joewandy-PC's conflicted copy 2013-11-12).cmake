# Install script for directory: /home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "/usr/local")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "Release")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

# Install shared libraries without execute permission?
IF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  SET(CMAKE_INSTALL_SO_NO_EXE "1")
ENDIF(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/vigra" TYPE FILE FILES
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/windows.h"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/eigensystem.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/tiff.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/basicimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_resize.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/imageiteratoradapter.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/distancetransform.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/diff2d.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/tinyvector.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/slanted_edge_mtf.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/rgbvalue.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/codec.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/regression.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/seededregiongrowing.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/labelvolume.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/stdimagefunctions.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/labelimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/mathutil.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/watersheds3d.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/stdconvolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/contourcirculator.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/affinegeometry.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/matlab_FLEXTYPE.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/separableconvolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/polynomial.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/imagecontainer.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/transformimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/edgedetection.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_iterator.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/interpolating_accessor.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/nonlineardiffusion.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/combineimages.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_array.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/matrix.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/fftw3.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/convolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/recursiveconvolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/matlab.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/pixelneighborhood.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/seededregiongrowing_extensions.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/flatmorphology.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/union_find.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/exclusive_lars.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/basicimageview.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/cellconfigurations.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/resizeimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/watersheds.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/tensorutilities.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/fftw.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/gaussians.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/voxelneighborhood.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/fixedpoint.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/tuple.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/accessor.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/seededregiongrowing3d.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/impex.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/colorconversions.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/static_assert.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/noise_normalization.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/inspectimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/utilities.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/rational.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/basicgeometry.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/config.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/rfftw.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/random_forest.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_convolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/error.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/memory.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/iteratortags.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_distance.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/random.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/splineimageview.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/gaborfilter.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_morphology.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/numerictraits.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/iteratoradapter.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/bordertreatment.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/orientedtensorfilters.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/singular_value_decomposition.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/imageiterator.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/stdimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/gradient_energy_tensor.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/linear_algebra.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/cornerdetection.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/boundarytensor.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/functorexpression.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/metaprogramming.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/navigator.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_pointoperators.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/copyimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/localminmax.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/functortraits.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/meshgrid.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/iteratortraits.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/recursiveconvolution_extensions.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/resampling_convolution.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/multi_impex.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/affine_registration.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/array_vector.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/cellimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/linear_solve.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/quadprog.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/imageinfo.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/promote_traits.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/initimage.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/splines.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/symmetry.hxx"
    "/home/joewandy/Project/real_datasets/SIMA_full/lib/vigra/include/vigra/sized_int.hxx"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

