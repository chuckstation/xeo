#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

void* av_frame_alloc(void) { return NULL; }
void av_frame_free(void** frame) {}
void av_free(void* ptr) {}
void av_log_set_callback(void* callback) {}
void* av_packet_alloc(void) { return NULL; }
void* avcodec_alloc_context3(const void* codec) { return NULL; }
int avcodec_close(void* avctx) { return 0; }
void* avcodec_find_decoder(int id) { return NULL; }
int avcodec_is_open(void* s) { return 0; }
int avcodec_open2(void* avctx, const void* codec, void** options) { return 0; }
int avcodec_receive_frame(void* avctx, void* frame) { return 0; }
int avcodec_send_packet(void* avctx, const void* avpkt) { return 0; }

void* av_md5_alloc(void) { return NULL; }
void av_md5_init(void* ctx) {}
void av_md5_update(void* ctx, const unsigned char* src, size_t len) {}
void av_md5_final(void* ctx, unsigned char* dst) {}

void* av_sha512_alloc(void) { return NULL; }
void av_sha512_init(void* ctx, int bits) {}
void av_sha512_update(void* ctx, const unsigned char* src, size_t len) {}
void av_sha512_final(void* ctx, unsigned char* dst) {}

void avcodec_free_context(void** avctx) {}
void* av_buffer_alloc(size_t size) { return NULL; }
int av_strerror(int errnum, char* errbuf, size_t errbuf_size) { return 0; }
void av_channel_layout_default(void* ch_layout, int nb_channels) {}

#ifdef __cplusplus
}
#endif
