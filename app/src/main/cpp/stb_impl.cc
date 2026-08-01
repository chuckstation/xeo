#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "third_party/stb/stb_image_write.h"
#include <stddef.h>

extern "C" {
size_t ZSTD_compress(void* dst, size_t dstCapacity, const void* src, size_t srcSize, int compressionLevel) {
  return 0;
}

size_t ZSTD_compressBound(size_t srcSize) {
  return 0;
}

size_t ZSTD_decompress(void* dst, size_t dstCapacity, const void* src, size_t compressedSize) {
  return 0;
}
}
