#ifndef __CDK15_TYPES_STRUCTURED_TYPE_H__
#define __CDK15_TYPES_STRUCTURED_TYPE_H__

#include <vector>
#include <numeric>
#include <cdk/types/basic_type.h>

namespace cdk {

  /**
   * This class represents a structured type concept.
   */
  class structured_type: public basic_type {
    std::vector<std::shared_ptr<basic_type>> _components;

  private:
    size_t compute_size(const std::vector<std::shared_ptr<basic_type>> &components) {
      size_t size = 0;
      for (auto component : components)
        size += component->size();
      return size;
    }

  public:

    explicit structured_type(explicit_call_disabled, const std::vector<std::shared_ptr<basic_type>> &components) :
        basic_type(compute_size(components), TYPE_STRUCT), _components(components) {
      // EMPTY
    }

    ~structured_type() = default;

  public:

    std::shared_ptr<basic_type> component(size_t ix) {
      return _components[ix];
    }

    const std::vector<std::shared_ptr<basic_type>>& components() const {
      return _components;
    }

    size_t length() const {
      return _components.size();
    }

  public:

    static auto create(const std::vector<std::shared_ptr<basic_type>> &types) {
      return std::make_shared<structured_type>(explicit_call_disabled(), types);
    }

    static auto cast(std::shared_ptr<basic_type> type) {
      return std::dynamic_pointer_cast<structured_type>(type);
    }

  };

} // cdk

#endif
