#ifndef CUSTOM_ZLIB_NG_H_
#define CUSTOM_ZLIB_NG_H_

#include <zlib.h>

#define zng_stream z_stream
#define zng_inflateInit2(strm, windowBits) inflateInit2(strm, windowBits)
#define zng_inflate(strm, flush) inflate(strm, flush)
#define zng_inflateEnd(strm) inflateEnd(strm)

#endif // CUSTOM_ZLIB_NG_H_
