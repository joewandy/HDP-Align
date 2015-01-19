/*
 * config.h
 *
 * Copyright (c) 2009 automatically generated
 *
 * This file is part of ms++.
 *
 * ms++ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ms++ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ms++. If not, see <http://www.gnu.org/licenses/>.
 *
 */

#ifndef __CONFIG_H__
#define __CONFIG_H__

#ifdef _WIN32
#define DELIM  "\\"
#else
#define DELIM  "/"
#endif

#ifdef _MSC_VER
	#include "winsock2.h"
	#include "vigra/windows.h"
#endif

#ifdef min
#undef min
#endif
#ifdef max
#undef max
#endif

/* #undef HAVE_UNIX_ISNAN */
/* #undef HAVE_UNIX_ISINF */

#ifdef _WIN32
	#define MSPP_EXPORT __declspec( dllexport )
	/* Disable a template related MSVC warning.
	   See: http://www.unknownroad.com/rtfm/VisualStudio/warningC4251.html */
	#pragma warning( disable: 4251 )
#else
	#define MSPP_EXPORT
#endif

/*From file qlobal.h from Qt: Use this to avoid unsued variable warnings*/
#define MSPP_UNUSED(x) (void)x;

#endif

