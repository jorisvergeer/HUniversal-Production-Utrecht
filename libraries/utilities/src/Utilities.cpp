/**
 * @file Utilities.cpp
 * @brief Miscellaneous utilities.
 *
 * @author Lukas Vermond
 * @author Kasper van Nieuwland
 *
 * @section LICENSE
 * License: newBSD
 * 
 * Copyright © 2012, HU University of Applied Sciences Utrecht.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

#include <Utilities/Utilities.h>

namespace Utilities{
    /**
     * Get the current time in milliseconds.
     *
     * @return time in milliseconds.
     **/
    long timeNow(void){
        boost::posix_time::ptime time = boost::posix_time::microsec_clock::local_time();
        boost::posix_time::time_duration duration(time.time_of_day());
        return duration.total_milliseconds();
    }
    
    /**
     * Sleep for X milliseconds.
     *
     * @param milliseconds time in milliseconds.
     **/
    void sleep(long milliseconds){
        boost::this_thread::sleep(boost::posix_time::milliseconds(milliseconds));
    }

    /**
     * Converts radians to degrees.
     *
     * @param rad Amount of radians.
     *
     * @return degrees.
     **/
    double radiansToDegrees(double radians){
		return (radians / M_PI) * 180;
	}

    /**
     * Converts degrees to radians.
     *
     * @param deg Amount of degrees.
     *
     * @return radians.
     **/
	double degreesToRadians(double degrees){
		return (degrees / 180) * M_PI;
	}
}
