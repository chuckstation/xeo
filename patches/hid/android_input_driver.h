/**
 ******************************************************************************
 * Xenia : Xbox 360 Emulator Research Project                                 *
 ******************************************************************************
 * Copyright 2026 Xeo Contributors. All rights reserved.                     *
 * Released under the BSD license - see LICENSE in the root for more details. *
 ******************************************************************************
 */

#ifndef XENIA_HID_ANDROID_INPUT_DRIVER_H_
#define XENIA_HID_ANDROID_INPUT_DRIVER_H_

#include "xenia/hid/input_driver.h"
#include <mutex>
#include <cstring>

namespace xe {
namespace hid {

class AndroidInputDriver : public InputDriver {
 public:
  AndroidInputDriver(xe::ui::Window* window, size_t window_z_order);
  virtual ~AndroidInputDriver();

  X_STATUS Setup() override;

  X_RESULT GetCapabilities(uint32_t user_index, uint32_t flags,
                           X_INPUT_CAPABILITIES* out_caps) override;
  X_RESULT GetState(uint32_t user_index, X_INPUT_STATE* out_state) override;
  X_RESULT SetState(uint32_t user_index,
                    X_INPUT_VIBRATION* vibration) override;
  X_RESULT GetKeystroke(uint32_t user_index, uint32_t flags,
                        X_INPUT_KEYSTROKE* out_keystroke) override;

  static void SetButtonState(uint16_t buttons, uint8_t lt, uint8_t rt,
                             int16_t lx, int16_t ly, int16_t rx, int16_t ry);

 private:
  static std::mutex state_mutex_;
  static X_INPUT_STATE shared_state_;
};

}  // namespace hid
}  // namespace xe

#endif  // XENIA_HID_ANDROID_INPUT_DRIVER_H_
