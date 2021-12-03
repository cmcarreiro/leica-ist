#ifndef __CDK15_TYPES_TYPES_H__
#define __CDK15_TYPES_TYPES_H__

#include <cdk/types/basic_type.h>
#include <cdk/types/primitive_type.h>
#include <cdk/types/reference_type.h>
#include <cdk/types/structured_type.h>
#include <memory>

namespace cdk {

#if 0
  inline auto make_primitive_type(size_t size, typename_type name) {
    return std::make_shared<primitive_type>(size, name);
  }

  inline auto make_reference_type(size_t size, std::shared_ptr<basic_type> referenced) {
    return std::make_shared<reference_type>(size, referenced);
  }

  inline auto make_structured_type(const std::vector<std::shared_ptr<basic_type>> &types) {
    return std::make_shared<structured_type>(types);
  }

  inline auto reference_type_cast(std::shared_ptr<basic_type> type) {
    return std::dynamic_pointer_cast<reference_type>(type);
  }

  inline auto structured_type_cast(std::shared_ptr<basic_type> type) {
    return std::dynamic_pointer_cast<structured_type>(type);
  }
#endif

  inline std::string to_string(std::shared_ptr<basic_type> type) {
    if (type->name() == TYPE_INT) return "integer";
    if (type->name() == TYPE_DOUBLE) return "double";
    if (type->name() == TYPE_STRING) return "string";
    if (type->name() == TYPE_VOID) return "void";
    if (type->name() == TYPE_POINTER) {
      auto r = cdk::reference_type::cast(type)->referenced();
      return "pointer to " + to_string(r);
    } else {
      return "(unknown or unsupported type)";
    }
  }

} // cdk

#endif
