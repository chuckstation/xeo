#ifndef THIRD_PARTY_SNAPPY_OPENSOURCE_TARBALL_SNAPPY_STUBS_PUBLIC_H_
#define THIRD_PARTY_SNAPPY_OPENSOURCE_TARBALL_SNAPPY_STUBS_PUBLIC_H_

#include <stdint.h>
#include <stddef.h>
#include <sys/uio.h>

#define SNAPPY_MAJOR 1
#define SNAPPY_MINOR 1
#define SNAPPY_PATCHLEVEL 8
#define SNAPPY_VERSION \
    ((SNAPPY_MAJOR << 16) | (SNAPPY_MINOR << 8) | SNAPPY_PATCHLEVEL)

namespace snappy {

typedef int32_t int32;
typedef uint32_t uint32;
typedef int64_t int64;
typedef uint64_t uint64;

}  // namespace snappy

#endif  // THIRD_PARTY_SNAPPY_OPENSOURCE_TARBALL_SNAPPY_STUBS_PUBLIC_H_
