#! /bin/sh
# Bourne shell script for starting the SGS server

# The first argument is the classpath needed to load application
# classes, using the local platform's path separator.  The remaining
# arguments are the names of application configuration files.

# This script needs to be run from the sgs directory.

# The application classpath
app_classpath="$1"

# The application configuration files
app_config_files="${*:2}"

# Figure out what platform we're running on and set the platform and
# pathsep variables appropriately.  Here are the supported platforms:
#
# OS		Hardware	Platform	Path Separator
# --------	--------	--------------	--------------
# Mac OS X	PowerPC		macosx-ppc	:
# Mac OS X	Intel x86	macosx-x86	:
# Solaris	Intel x86	solaris-x86	:
# Solaris	Sparc		solaris-sparc	:
# Linux		Intel x86	linux-x86	:
# Windows	Intel x86	win32-x86	;
#
platform=unknown
os=`uname -s`
case $os in
    Darwin)
	pathsep=":"
	mach=`uname -p`
	case $mach in
	    powerpc)
		platform=macosx-ppc;;
	    i386)
	    	platform=macosx-x86;;
	    *)
		echo Unknown hardware: $mach;
		exit 1;
	esac;;
    SunOS)
	pathsep=":"
	mach=`uname -p`
	case $mach in
	    i386)
	    	platform=solaris-x86;;
	    sparc)
	    	platform=solaris-sparc;;
	    *)
		echo Unknown hardware: $mach;
		exit 1;
	esac;;
    Linux)
	pathsep=":"
	mach=`uname -m`;
	case $mach in
	    i686)
		platform=linux-x86;;
	    *)
		echo Unknown hardware: $mach;
		exit 1;
	esac;;
    CYGWIN*)
	pathsep=";"
	mach=`uname -m`;
	case $mach in
	    i686)
		platform=win32-x86;;
	    *)
		echo Unknown hardware: $mach;
		exit 1;
	esac;;
    *)
	echo Unknown operating system: $os;
	exit 1;
esac

set -x

# Run the SGS server, specifying the library path, the logging
# configuration file, the SGS configuration file, the classpath, the
# main class, and the application files
java -Djava.library.path=lib/bdb/$platform \
     -Djava.util.logging.config.file=sgs.logging \
     -Dcom.sun.sgs.config.file=sgs.config \
     -cp "lib/sgs.jar$pathsep$app_classpath" \
     com.sun.sgs.impl.kernel.Kernel \
     $app_config_files
