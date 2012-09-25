# ************************************************
# NEWER CLEAN STEPS MUST BE AT THE END OF THE LIST
# ************************************************

# For example:
# $(call add-clean-step, rm -rf $(OUT_DIR)/target/common/obj/APPS/AndroidTests_intermediates)
# $(call add-clean-step, rm -rf $(OUT_DIR)/target/common/obj/JAVA_LIBRARIES/core_intermediates)
# $(call add-clean-step, find $(OUT_DIR) -type f -name "IGTalkSession*" -print0 | xargs -0 rm -f)
# $(call add-clean-step, rm -rf $(PRODUCT_OUT)/data/*)

$(call add-clean-step, rm -rf $(OUT_DIR)/target/common/obj/JAVA_LIBRARIES/libGson_intermediates)
$(call add-clean-step, rm -rf $(OUT_DIR)/target/common/obj/APPS/RomUpdater_intermediates)
$(call add-clean-step, rm -rf $(OUT_DIR)/target/common/R/org/elegosproject)
$(call add-clean-step, rm -rf $(PRODUCT_OUT)/obj/APPS/RomUpdater_intermediates)

