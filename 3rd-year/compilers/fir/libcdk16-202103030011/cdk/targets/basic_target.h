#ifndef __CDK15_TARGETS_BASIC_TARGET_H__
#define __CDK15_TARGETS_BASIC_TARGET_H__

#include <memory>
#include <map>
#include <string>

namespace cdk {

  class compiler;

  class basic_target {
    /**
     * This is the registry for all evaluators, indexed by target.
     * Subclasses register their instances here through calls to the
     * superclass constructor.
     */
    static basic_target *&targets_by_name(const std::string &target) {
      static std::map<std::string, basic_target*> _targets_by_name;
      return _targets_by_name[target];
    }

  public:
    /**
     * How to get an evaluator for a given target.
     * @param target the target name: "asm", "c", "xml", etc.
     * @return a pointer to the evaluator object
     */
    static basic_target *get_target_for(const std::string &target) {
      return targets_by_name(target);
    }

  protected:
    basic_target(const char *target) {
      targets_by_name(target) = this;
    }

  public:
    //! How to destroy an evaluator.
    virtual ~basic_target() {
    }

  public:
    /**
     * Evaluation algorithm for a syntax tree: processes the
     * tree and sends the result to the output stream.
     * @param compiler object representing the compiler as a whole
     * @return true if the operation is successful
     */
    virtual bool evaluate(std::shared_ptr<compiler>) = 0;

  };

} // cdk

#endif
