/**
 ******************************************************************************
 * Xenia : Xbox 360 Emulator Research Project                                 *
 ******************************************************************************
 * Copyright 2020 Ben Vanik. All rights reserved.                             *
 * Released under the BSD license - see LICENSE in the root for more details. *
 ******************************************************************************
 */

#ifndef XENIA_BASE_UTF8_H_
#define XENIA_BASE_UTF8_H_

#include <string>
#include <vector>

#include "xenia/base/platform.h"

namespace xe::utf8 {

size_t count(const std::string_view view);

std::string lower_ascii(const std::string_view view);
std::string upper_ascii(const std::string_view view);

size_t hash_fnv1a(const std::string_view view);
size_t hash_fnv1a_case(const std::string_view view);

// Splits the given haystack on any delimiters (needles) and returns all parts.
std::vector<std::string_view> split(const std::string_view haystack,
                                    const std::string_view needles,
                                    bool remove_empty = false);

bool equal_z(const std::string_view left, const std::string_view right);

bool equal_case(const std::string_view left, const std::string_view right);

bool equal_case_z(const std::string_view left, const std::string_view right);

std::string_view::size_type find_any_of(const std::string_view haystack,
                                        const std::string_view needles);

std::string_view::size_type find_any_of_case(const std::string_view haystack,
                                             const std::string_view needles);

std::string_view::size_type find_first_of(const std::string_view haystack,
                                          const std::string_view needle);

// find_first_of string, case insensitive.
std::string_view::size_type find_first_of_case(const std::string_view haystack,
                                               const std::string_view needle);

bool starts_with(const std::string_view haystack,
                 const std::string_view needle);

bool starts_with_case(const std::string_view haystack,
                      const std::string_view needle);

bool ends_with(const std::string_view haystack, const std::string_view needle);

bool ends_with_case(const std::string_view haystack,
                    const std::string_view needle);

// Splits the given path on any valid path separator and returns all parts.
std::vector<std::string_view> split_path(const std::string_view path);

// Joins two path segments with the given separator.
std::string join_paths(const std::string_view left_path,
                       const std::string_view right_path,
                       char32_t separator = kPathSeparator);

std::string join_paths(const std::vector<std::string>& paths,
                       char32_t separator = kPathSeparator);

std::string join_paths(const std::vector<std::string_view>& paths,
                       char32_t separator = kPathSeparator);

inline std::string join_paths(
    std::initializer_list<const std::string_view> paths,
    char32_t separator = kPathSeparator) {
  std::string result;
  for (auto path : paths) {
    result = join_paths(result, path, separator);
  }
  return result;
}

inline std::string join_guest_paths(const std::string_view left_path,
                                    const std::string_view right_path) {
  return join_paths(left_path, right_path, kGuestPathSeparator);
}

inline std::string join_guest_paths(const std::vector<std::string>& paths) {
  return join_paths(paths, kGuestPathSeparator);
}

inline std::string join_guest_paths(
    const std::vector<std::string_view>& paths) {
  return join_paths(paths, kGuestPathSeparator);
}

inline std::string join_guest_paths(
    std::initializer_list<const std::string_view> paths) {
  return join_paths(paths, kGuestPathSeparator);
}

// Replaces all path separators with the given value and removes redundant
// separators.
std::string fix_path_separators(const std::string_view path,
                                char32_t new_separator = kPathSeparator);

inline std::string fix_guest_path_separators(const std::string_view path) {
  return fix_path_separators(path, kGuestPathSeparator);
}

// Find the top directory name or filename from a path.
std::string find_name_from_path(const std::string_view path,
                                char32_t separator = kPathSeparator);

inline std::string find_name_from_guest_path(const std::string_view path) {
  return find_name_from_path(path, kGuestPathSeparator);
}

std::string find_base_name_from_path(const std::string_view path,
                                     char32_t separator = kPathSeparator);

inline std::string find_base_name_from_guest_path(const std::string_view path) {
  return find_base_name_from_path(path, kGuestPathSeparator);
}

// Get parent path of the given directory or filename.
std::string find_base_path(const std::string_view path,
                           char32_t separator = kPathSeparator);

inline std::string find_base_guest_path(const std::string_view path) {
  return find_base_path(path, kGuestPathSeparator);
}

// Canonicalizes a path, removing ..'s.
std::string canonicalize_path(const std::string_view path,
                              char32_t separator = kPathSeparator);

inline std::string canonicalize_guest_path(const std::string_view path) {
  return canonicalize_path(path, kGuestPathSeparator);
}

#ifdef __cpp_char8_t

// Helper to convert std::u8string_view to std::string_view
inline std::string_view ToSV(std::u8string_view u8view) {
  return std::string_view(reinterpret_cast<const char*>(u8view.data()), u8view.size());
}

inline size_t count(const std::u8string_view view) {
  return count(ToSV(view));
}

inline std::string lower_ascii(const std::u8string_view view) {
  return lower_ascii(ToSV(view));
}

inline std::string upper_ascii(const std::u8string_view view) {
  return upper_ascii(ToSV(view));
}

inline size_t hash_fnv1a(const std::u8string_view view) {
  return hash_fnv1a(ToSV(view));
}

inline size_t hash_fnv1a_case(const std::u8string_view view) {
  return hash_fnv1a_case(ToSV(view));
}

inline std::vector<std::string_view> split(const std::u8string_view haystack,
                                           const std::u8string_view needles,
                                           bool remove_empty = false) {
  return split(ToSV(haystack), ToSV(needles), remove_empty);
}
inline std::vector<std::string_view> split(const std::string_view haystack,
                                           const std::u8string_view needles,
                                           bool remove_empty = false) {
  return split(haystack, ToSV(needles), remove_empty);
}
inline std::vector<std::string_view> split(const std::u8string_view haystack,
                                           const std::string_view needles,
                                           bool remove_empty = false) {
  return split(ToSV(haystack), needles, remove_empty);
}

inline bool equal_z(const std::u8string_view left, const std::u8string_view right) {
  return equal_z(ToSV(left), ToSV(right));
}
inline bool equal_z(const std::string_view left, const std::u8string_view right) {
  return equal_z(left, ToSV(right));
}
inline bool equal_z(const std::u8string_view left, const std::string_view right) {
  return equal_z(ToSV(left), right);
}

inline bool equal_case(const std::u8string_view left, const std::u8string_view right) {
  return equal_case(ToSV(left), ToSV(right));
}
inline bool equal_case(const std::string_view left, const std::u8string_view right) {
  return equal_case(left, ToSV(right));
}
inline bool equal_case(const std::u8string_view left, const std::string_view right) {
  return equal_case(ToSV(left), right);
}

inline bool equal_case_z(const std::u8string_view left, const std::u8string_view right) {
  return equal_case_z(ToSV(left), ToSV(right));
}
inline bool equal_case_z(const std::string_view left, const std::u8string_view right) {
  return equal_case_z(left, ToSV(right));
}
inline bool equal_case_z(const std::u8string_view left, const std::string_view right) {
  return equal_case_z(ToSV(left), right);
}

inline std::string_view::size_type find_any_of(const std::u8string_view haystack,
                                               const std::u8string_view needles) {
  return find_any_of(ToSV(haystack), ToSV(needles));
}
inline std::string_view::size_type find_any_of(const std::string_view haystack,
                                               const std::u8string_view needles) {
  return find_any_of(haystack, ToSV(needles));
}
inline std::string_view::size_type find_any_of(const std::u8string_view haystack,
                                               const std::string_view needles) {
  return find_any_of(ToSV(haystack), needles);
}

inline std::string_view::size_type find_any_of_case(const std::u8string_view haystack,
                                                    const std::u8string_view needles) {
  return find_any_of_case(ToSV(haystack), ToSV(needles));
}
inline std::string_view::size_type find_any_of_case(const std::string_view haystack,
                                                    const std::u8string_view needles) {
  return find_any_of_case(haystack, ToSV(needles));
}
inline std::string_view::size_type find_any_of_case(const std::u8string_view haystack,
                                                    const std::string_view needles) {
  return find_any_of_case(ToSV(haystack), needles);
}

inline std::string_view::size_type find_first_of(const std::u8string_view haystack,
                                                 const std::u8string_view needle) {
  return find_first_of(ToSV(haystack), ToSV(needle));
}
inline std::string_view::size_type find_first_of(const std::string_view haystack,
                                                 const std::u8string_view needle) {
  return find_first_of(haystack, ToSV(needle));
}
inline std::string_view::size_type find_first_of(const std::u8string_view haystack,
                                                 const std::string_view needle) {
  return find_first_of(ToSV(haystack), needle);
}

inline std::string_view::size_type find_first_of_case(const std::u8string_view haystack,
                                                      const std::u8string_view needle) {
  return find_first_of_case(ToSV(haystack), ToSV(needle));
}
inline std::string_view::size_type find_first_of_case(const std::string_view haystack,
                                                      const std::u8string_view needle) {
  return find_first_of_case(haystack, ToSV(needle));
}
inline std::string_view::size_type find_first_of_case(const std::u8string_view haystack,
                                                      const std::string_view needle) {
  return find_first_of_case(ToSV(haystack), needle);
}

inline bool starts_with(const std::u8string_view haystack,
                        const std::u8string_view needle) {
  return starts_with(ToSV(haystack), ToSV(needle));
}
inline bool starts_with(const std::string_view haystack,
                        const std::u8string_view needle) {
  return starts_with(haystack, ToSV(needle));
}
inline bool starts_with(const std::u8string_view haystack,
                        const std::string_view needle) {
  return starts_with(ToSV(haystack), needle);
}

inline bool starts_with_case(const std::u8string_view haystack,
                             const std::u8string_view needle) {
  return starts_with_case(ToSV(haystack), ToSV(needle));
}
inline bool starts_with_case(const std::string_view haystack,
                             const std::u8string_view needle) {
  return starts_with_case(haystack, ToSV(needle));
}
inline bool starts_with_case(const std::u8string_view haystack,
                             const std::string_view needle) {
  return starts_with_case(ToSV(haystack), needle);
}

inline bool ends_with(const std::u8string_view haystack,
                      const std::u8string_view needle) {
  return ends_with(ToSV(haystack), ToSV(needle));
}
inline bool ends_with(const std::string_view haystack,
                      const std::u8string_view needle) {
  return ends_with(haystack, ToSV(needle));
}
inline bool ends_with(const std::u8string_view haystack,
                      const std::string_view needle) {
  return ends_with(ToSV(haystack), needle);
}

inline bool ends_with_case(const std::u8string_view haystack,
                           const std::u8string_view needle) {
  return ends_with_case(ToSV(haystack), ToSV(needle));
}
inline bool ends_with_case(const std::string_view haystack,
                           const std::u8string_view needle) {
  return ends_with_case(haystack, ToSV(needle));
}
inline bool ends_with_case(const std::u8string_view haystack,
                           const std::string_view needle) {
  return ends_with_case(ToSV(haystack), needle);
}

inline std::vector<std::string_view> split_path(const std::u8string_view path) {
  return split_path(ToSV(path));
}

inline std::string join_paths(const std::u8string_view left_path,
                              const std::u8string_view right_path,
                              char32_t separator = kPathSeparator) {
  return join_paths(ToSV(left_path), ToSV(right_path), separator);
}
inline std::string join_paths(const std::string_view left_path,
                              const std::u8string_view right_path,
                              char32_t separator = kPathSeparator) {
  return join_paths(left_path, ToSV(right_path), separator);
}
inline std::string join_paths(const std::u8string_view left_path,
                              const std::string_view right_path,
                              char32_t separator = kPathSeparator) {
  return join_paths(ToSV(left_path), right_path, separator);
}

inline std::string fix_path_separators(const std::u8string_view path,
                                       char32_t new_separator = kPathSeparator) {
  return fix_path_separators(ToSV(path), new_separator);
}

inline std::string find_name_from_path(const std::u8string_view path,
                                       char32_t separator = kPathSeparator) {
  return find_name_from_path(ToSV(path), separator);
}

inline std::string find_base_name_from_path(const std::u8string_view path,
                                            char32_t separator = kPathSeparator) {
  return find_base_name_from_path(ToSV(path), separator);
}

inline std::string find_base_path(const std::u8string_view path,
                                  char32_t separator = kPathSeparator) {
  return find_base_path(ToSV(path), separator);
}

inline std::string canonicalize_path(const std::u8string_view path,
                                     char32_t separator = kPathSeparator) {
  return canonicalize_path(ToSV(path), separator);
}

#endif

}  // namespace xe::utf8

#ifdef __cpp_char8_t
inline std::string operator+(const char8_t* lhs, const std::string& rhs) {
  return std::string(reinterpret_cast<const char*>(lhs)) + rhs;
}
inline std::string operator+(const std::string& lhs, const char8_t* rhs) {
  return lhs + reinterpret_cast<const char*>(rhs);
}
inline std::string operator+(std::string&& lhs, const char8_t* rhs) {
  return std::move(lhs) + reinterpret_cast<const char*>(rhs);
}
inline std::string operator+(const char8_t* lhs, std::string&& rhs) {
  return std::string(reinterpret_cast<const char*>(lhs)) + std::move(rhs);
}
inline std::string operator+(const std::u8string& lhs, const std::string& rhs) {
  return std::string(reinterpret_cast<const char*>(lhs.data()), lhs.size()) + rhs;
}
inline std::string operator+(const std::string& lhs, const std::u8string& rhs) {
  return lhs + std::string(reinterpret_cast<const char*>(rhs.data()), rhs.size());
}
inline std::string operator+(std::u8string_view lhs, const std::string& rhs) {
  return std::string(reinterpret_cast<const char*>(lhs.data()), lhs.size()) + rhs;
}
inline std::string operator+(const std::string& lhs, std::u8string_view rhs) {
  return lhs + std::string(reinterpret_cast<const char*>(rhs.data()), rhs.size());
}
inline std::string& operator+=(std::string& lhs, const char8_t* rhs) {
  lhs += reinterpret_cast<const char*>(rhs);
  return lhs;
}
inline std::string& operator+=(std::string& lhs, const std::u8string& rhs) {
  lhs.append(reinterpret_cast<const char*>(rhs.data()), rhs.size());
  return lhs;
}
inline std::string& operator+=(std::string& lhs, std::u8string_view rhs) {
  lhs.append(reinterpret_cast<const char*>(rhs.data()), rhs.size());
  return lhs;
}
#endif

#endif  // XENIA_BASE_UTF8_H_
