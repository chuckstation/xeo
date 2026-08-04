/**
 ******************************************************************************
 * Xenia : Xbox 360 Emulator Research Project                                 *
 ******************************************************************************
 * Copyright 2026 Xeo Contributors. All rights reserved.                     *
 * Released under the BSD license - see LICENSE in the root for more details. *
 ******************************************************************************
 */

#include "xenia/hid/android_input_driver.h"

namespace xe {
namespace hid {

std::mutex AndroidInputDriver::state_mutex_;
X_INPUT_STATE AndroidInputDriver::shared_state_ = {0};

AndroidInputDriver::AndroidInputDriver(xe::ui::Window* window, size_t window_z_order)
    : InputDriver(window, window_z_order) {}

AndroidInputDriver::~AndroidInputDriver() = default;

X_STATUS AndroidInputDriver::Setup() {
  return X_STATUS_SUCCESS;
}

X_RESULT AndroidInputDriver::GetCapabilities(uint32_t user_index, uint32_t flags,
                                             X_INPUT_CAPABILITIES* out_caps) {
  if (user_index != 0) {
    return X_ERROR_DEVICE_NOT_CONNECTED;
  }
  std::memset(out_caps, 0, sizeof(X_INPUT_CAPABILITIES));
  out_caps->type = 0x01; // XINPUT_DEVTYPE_GAMEPAD
  out_caps->sub_type = 0x01; // XINPUT_DEVSUBTYPE_GAMEPAD
  out_caps->flags = 0x00;
  return X_ERROR_SUCCESS;
}

X_RESULT AndroidInputDriver::GetState(uint32_t user_index, X_INPUT_STATE* out_state) {
  if (user_index != 0) {
    return X_ERROR_DEVICE_NOT_CONNECTED;
  }
  std::lock_guard<std::mutex> lock(state_mutex_);
  std::memcpy(out_state, &shared_state_, sizeof(X_INPUT_STATE));
  return X_ERROR_SUCCESS;
}

X_RESULT AndroidInputDriver::SetState(uint32_t user_index,
                                      X_INPUT_VIBRATION* vibration) {
  if (user_index != 0) {
    return X_ERROR_DEVICE_NOT_CONNECTED;
  }
  return X_ERROR_SUCCESS;
}

X_RESULT AndroidInputDriver::GetKeystroke(uint32_t user_index, uint32_t flags,
                                          X_INPUT_KEYSTROKE* out_keystroke) {
  return X_ERROR_EMPTY;
}

void AndroidInputDriver::SetButtonState(uint16_t buttons, uint8_t lt, uint8_t rt,
                                        int16_t lx, int16_t ly, int16_t rx, int16_t ry) {
  std::lock_guard<std::mutex> lock(state_mutex_);
  shared_state_.packet_number = shared_state_.packet_number + 1;
  shared_state_.gamepad.buttons = buttons;
  shared_state_.gamepad.left_trigger = lt;
  shared_state_.gamepad.right_trigger = rt;
  shared_state_.gamepad.thumb_lx = lx;
  shared_state_.gamepad.thumb_ly = ly;
  shared_state_.gamepad.thumb_rx = rx;
  shared_state_.gamepad.thumb_ry = ry;
}

}  // namespace hid
}  // namespace xe
